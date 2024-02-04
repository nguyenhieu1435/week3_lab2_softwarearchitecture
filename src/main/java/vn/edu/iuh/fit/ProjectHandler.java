package vn.edu.iuh.fit;


import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

public class ProjectHandler {
    private File projectFile;
    private FileWriter outputFileWritter;
    private File outFile;
    
    public ProjectHandler(String projectPath, String outputFilePath) throws IOException{
        this.projectFile = new File(projectPath);
        this.outFile = new File(outputFilePath);
       
    }
    
    public void run() throws IOException{
        boolean projectCanRead = projectFile.canRead();
        boolean outputFileCanWrite = outFile.canWrite();
        if (!projectCanRead){
            JOptionPane.showMessageDialog(null, "You have to grant read permission for project file", "Notice!", JOptionPane.INFORMATION_MESSAGE);
            
        }
        if (!outputFileCanWrite){
            JOptionPane.showMessageDialog(null, "You have to grant write permission for output file", "Notice!", JOptionPane.INFORMATION_MESSAGE);
            
        }

        if (projectCanRead && outputFileCanWrite){
            outputFileWritter = new FileWriter(outFile);
            new DirExplorer(ProjectHandler::filterByPath
                    , (level, path, file ) -> handleFile(level, path, file, Optional.of(outputFileWritter))).explore(projectFile);
            outputFileWritter.close();
            System.out.println("Finish checking!");
        }
        
    }

    public static boolean filterByPath(int level, String path, File file) {
        // add path filter at here
        return path.endsWith(".java");
    }

    public static void handleFile(int level, String path, File file, Optional<FileWriter> reportWriterOpt) {
        FileWriter reportWriter = reportWriterOpt.orElse(null);
        if (reportWriter == null){
            System.out.println("\n\n\nBegin check file: " + path);
        } else {
            try {
                reportWriter.write("\n\n\nBegin check file: " + path + "\n");
            } catch (IOException ex) {
                Logger.getLogger(ProjectHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
        try {
            new VoidVisitorAdapter<Object>() {

                @Override
                public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                    // TODO Auto-generated method stub
                    
                    try {
                        handleClassName(n, arg, Optional.of(reportWriter));
                        handleCommentForClass(n, arg, Optional.of(reportWriter));
                        handleFieldName(n, arg, Optional.of(reportWriter));
                        handleConstField(n, arg, Optional.of(reportWriter));
                        handleMethodName(n, arg, Optional.of(reportWriter));
                        handleDocCommentForMethod(n, arg, Optional.of(reportWriter));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                   
                }
            }.visit(StaticJavaParser.parse(file), null);

            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(CompilationUnit n, Object arg) {
                    try {
                        handlePackageName(n, arg, Optional.of(reportWriter));
                    } catch (IOException ex) {
                        Logger.getLogger(ProjectHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.visit(StaticJavaParser.parse(file), null);


        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }
        if (reportWriter == null){
            System.out.println("End check file: " + path + "\n\n");
        } else {
            try {
                reportWriter.write("End check file: " + path + "\n\n\n");
            } catch (IOException ex) {
                Logger.getLogger(ProjectHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // 1.  Các package trong dự án phải theo mẫu: com.companyname.* (*:tên bất kỳ)
    public static void handlePackageName(CompilationUnit cu, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException {
        Optional<PackageDeclaration> optional = cu.getPackageDeclaration();
        if (optional.isPresent()) {
            PackageDeclaration packageDeclaration = optional.get();

            String packageName = packageDeclaration.getNameAsString();
            if (!packageName.matches("^com\\.companyname\\..*")) {
                handleOutputFileOrConsole(reportWriterOptional,
                    "########## Invalid: Package name: " + packageName + ", at line" 
                            + Objects.requireNonNull(packageDeclaration.getBegin().orElse(null)).line    
                );
            }
        }


    }

    // 2.Các class phải có tên là một danh từ hoặc cụm danh ngữ và phải bắt đầu bằng chữ hoa.
    public static void handleClassName(ClassOrInterfaceDeclaration n, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException {
        String clsName = n.getNameAsString();
        char firstWord = clsName.charAt(0);

        String finalClassname = convertCamelCaseToRealName(clsName);

        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens = tokenizer.tokenize(finalClassname);
        InputStream inputStreamPOSTagger = new FileInputStream("models/en-pos-maxent.bin");


        POSModel posModel = new POSModel(inputStreamPOSTagger);
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String tags[] = posTagger.tag(tokens);

        InputStream inputStreamChunker = new FileInputStream(("models/en-chunker.bin"));
        ChunkerModel chunkerModel
                = new ChunkerModel(inputStreamChunker);
        ChunkerME chunker = new ChunkerME(chunkerModel);
        String[] chunks = chunker.chunk(tokens, tags);

        for (String chunk : chunks){
            // if not word chunk is empty string
            if (!chunk.contains("N") || chunk.trim().isEmpty()){
                handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: ClassName: " 
                        + clsName + ", class name must be noun or noun phrase"
                );
            }
        }

        if (firstWord <= 'z' && firstWord >= 'a') {
            Optional<Position> opBeginPostion = n.getBegin();
            Optional<Position> opEndPosition = n.getEnd();

            if (opBeginPostion.isPresent() && opEndPosition.isPresent()) {
                handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: ClassName: " 
                        + clsName + " is invalid, error at line: "
                        + opBeginPostion.get().line);
                
            } else {
                handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: ClassName: " + clsName + " is invalid");
              
            }
        }
    }

    public static String convertCamelCaseToRealName(String clsName) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < clsName.length(); i++) {
            if (i != 0 && clsName.charAt(i) >= 'A' && clsName.charAt(i) <= 'Z') {
                stringBuilder.append(" ");
                stringBuilder.append(clsName.charAt(i));
            } else {
                stringBuilder.append(clsName.charAt(i));
            }
        }
        return stringBuilder.toString();
    }

    //	3.  Mỗi lớp phải có một comment mô tả cho lớp. Trong comment đó phải có ngày tạo
    // (created-date) và author
    public static void handleCommentForClass(ClassOrInterfaceDeclaration coid, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException {
        String clsName = coid.getNameAsString();
        Optional<Comment> optional = coid.getComment();

        if (optional.isPresent()) {
            Comment comment = optional.get();
            if (!comment.getContent().toLowerCase().contains("author")) {
                handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: Classname: " 
                        + clsName + " is invalid, comment hasn't author");      
            }
            if (!comment.getContent().toLowerCase().contains("created-date")) {
                handleOutputFileOrConsole(reportWriterOptional
                        , "########## Invalid: Classname: " + clsName + " is invalid, comment hasn't create-date");
            }
        } else {
            handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: Classname: " + clsName + " is invalid, this class hasn't comment!");
        }
    }
    /*
        4. Các fields trong các class phải là danh từ hoặc cụm danh ngữ và phải bắt đầu bằng một
        chữ thường.
     */
    public static void handleFieldName(ClassOrInterfaceDeclaration n, Object args, Optional<FileWriter> reportWriterOptional) throws IOException {
        String clsName = n.getNameAsString();
        List<FieldDeclaration> fields = n.getFields();
        

        for (FieldDeclaration field : fields){
            String fieldName = field.getVariables().get(0).getNameAsString();
            if ((fieldName.charAt(0) < 'a' || fieldName.charAt(0) > 'z' ) && !field.isFinal()){
                handleOutputFileOrConsole(reportWriterOptional, "########## Invalid: FieldName: " + fieldName + " at Class: " + clsName 
                        + " first character must be lower case");
            }
            
            String finalFieldName = convertCamelCaseToRealName(fieldName);
            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
            String[] tokens = tokenizer.tokenize(finalFieldName);
            InputStream inputStreamPOSTagger = new FileInputStream("models/en-pos-maxent.bin");


            POSModel posModel = new POSModel(inputStreamPOSTagger);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            String tags[] = posTagger.tag(tokens);

            InputStream inputStreamChunker = new FileInputStream(("models/en-chunker.bin"));
            ChunkerModel chunkerModel
                    = new ChunkerModel(inputStreamChunker);
            ChunkerME chunker = new ChunkerME(chunkerModel);
            String[] chunks = chunker.chunk(tokens, tags);

            for (String chunk : chunks){
                // if not word chunk is empty string
                if (!chunk.contains("N") || chunk.trim().isEmpty()){
                    handleOutputFileOrConsole(reportWriterOptional
                            , "########## Invalid: FieldName: " + fieldName + " at Class: " 
                                    + clsName + ", field name must be noun or noun phrase");
                }
            }

        }


    }
    // 5. Tất cả các hằng số phải là chữ viết hoa và phải nằm trong một interface.
    public static void handleConstField(ClassOrInterfaceDeclaration n, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException{
        String clsName = n.getNameAsString();
        List<FieldDeclaration> fields = n.getFields();
        for (FieldDeclaration field : fields){

            if (field.isFinal()){
                String fieldName = field.getVariables().get(0).getNameAsString();
                if (!fieldName.equals(fieldName.toUpperCase())){

                    handleOutputFileOrConsole(reportWriterOptional, 
                    "########## Invalid: Const Field: " + fieldName + " at Class: " + clsName + ", field name must be Upper case!"
                    );
                }
                if (!n.isInterface()){
                    handleOutputFileOrConsole(reportWriterOptional, 
                        "########## Invalid: Const Field: " + fieldName + " at Class: " + clsName + ", field name must be in Interface not Class"
                    );             
                }
            }
        }
    }
//    6. Tên method phải bắt đầu bằng một động từ và phải là chữ thường
    public static void handleMethodName(ClassOrInterfaceDeclaration n, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException {
        String clsName = n.getNameAsString();
        List<MethodDeclaration> methods = n.getMethods();
        String finalMethodName = null;
        for (MethodDeclaration method : methods) {
            String nameAsString = method.getNameAsString();
            finalMethodName = convertCamelCaseToRealName(nameAsString);
            if (nameAsString.charAt(0) > 'z' || nameAsString.charAt(0) < 'a') {

                handleOutputFileOrConsole(reportWriterOptional
                , 
                  "########## Invalid: Method Name: " + nameAsString + " at Class: " + clsName + ", first character must be lower case"     
                );
            }

            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
            String[] tokens = tokenizer.tokenize(finalMethodName);
            InputStream inputStreamPOSTagger = new FileInputStream("models/en-pos-maxent.bin");


            POSModel posModel = new POSModel(inputStreamPOSTagger);
            POSTaggerME posTagger = new POSTaggerME(posModel);
            String tags[] = posTagger.tag(tokens);
            // if first word isn't verb -> handle
            // if first word is empty -> it isn't a word
            if ((tags[0].isEmpty() || !tags[0].contains("V")) && !nameAsString.equalsIgnoreCase("toString") ){    
                handleOutputFileOrConsole(reportWriterOptional, 
                  "########## Invalid: Method Name: " + nameAsString + " at Class: " + clsName + ", first word must be verb"
                );
            }

        }

    }
    // 7. Mỗi method phải có một ghi chú mô tả cho công việc của method trừ phương thức
    // default constructor, accessors/mutators, hashCode, equals, toString
    public static void handleDocCommentForMethod(ClassOrInterfaceDeclaration n, Object arg, Optional<FileWriter> reportWriterOptional) throws IOException{
        String clsName = n.getNameAsString();
        List<MethodDeclaration> methods = n.getMethods();
        for (MethodDeclaration method: methods){
            Optional<Comment> optionalComment = method.getComment();
            if (optionalComment.isPresent()
                &&
                   !method.getNameAsString().equalsIgnoreCase("toString")
                &&
                   !isGetter(method.getNameAsString(), method.getType().asString())
                &&
                   !isSetter(method.getNameAsString(), method.getType().asString())
                &&
                   !method.getNameAsString().equalsIgnoreCase("hashCode")
                &&  !method.getNameAsString().equalsIgnoreCase("equals")
            ){
                Comment comment = optionalComment.get();

                if (!comment.isJavadocComment() || comment.getContent().trim().isEmpty()){
                    handleOutputFileOrConsole(reportWriterOptional, 
                       "########## Invalid: Method: " + method.getNameAsString() + " at Class: " + clsName + ", method have to least 1 DocComment"
                    );
                  
                }

            }
        }
    }
    public static boolean isGetter(String methodName, String returnType){
        return methodName.startsWith("get") && !returnType.equals("void");
    }
    public static boolean isSetter(String methodName, String returnType){
        return methodName.startsWith("set") && returnType.equals("void");
    }
    private static void handleOutputFileOrConsole(Optional<FileWriter> fileWriterOpt, String text) throws IOException{
        FileWriter fileWriter = fileWriterOpt.orElse(null);
        if (fileWriter == null){
            System.err.println(text);
        } else {
            fileWriter.write(text + "\n");
        }
    }
}
