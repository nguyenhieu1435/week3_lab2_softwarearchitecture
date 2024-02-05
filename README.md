# Bài tập kiểm tra tính hợp lệ của Mã nguồn

## 🚩 Mục lục
- [Yêu cầu của bài tập](#yêu-cầu-của-bài-tập) 
- [Tóm tắt](#tóm-tắt)
- [Được xây dựng bằng](#được-xây-dựng-bằng)
- [Các dependency sử dụng](#các-Dependency-sử-dụng)
- [Demo Chương trình](#demo-Chương-trình)

## Yêu cầu của bài tập

- [Đặc tả yêu cầu](https://drive.google.com/file/d/1xtPY1ntHGO6Zq0iSLtxD1cx8Ax9bQ_oi/view?usp=sharing)
  + Preview
  ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/60c26ea1-d8a2-4c98-bb49-117d20eee891)

## Tóm tắt
- Sử dụng JavaParser để đọc được thông tin, thuộc tính của các Class; các Method, Variable, Package của Class đó.
- Sử dụng OpenNLP để phát hiện các từ có phải là động từ hay là danh từ; mô tả ý tưởng: tên classes, methods, variables đều được viết liền nên chúng ta tạo
1 hàm để biến tên camelCase về tên thật (VD: "personalInformation" -> "personal Information") tiếp đến ta sử dụng phương thức tokenize của SimpleTokenizer để tác 1 câu thành mảng
String các từ, tiếp đến sử dụng tag method của POSTaggerME để kiểm tra xem từ đó là loại từ nào (danh từ, động từ, v..v.. nếu nó là danh từ thì sẽ được ký hiểu với tag NN, muốn biết
  rõ hơn có thể xem [tại đây](https://dpdearing.com/posts/2011/12/opennlp-part-of-speech-pos-tags-penn-english-treebank/)). Ngoài ra còn kiếm tra Cụm danh từ bằng cách sử dụng chunk
   của ChunkerME để kiểm tra câu đó có phải là cụm danh từ hay không


## Được xây dựng bằng
<div align="center">
  <div>
    <img src="https://techstack-generator.vercel.app/java-icon.svg" alt="icon" width="50" height="50" />
    <p>Java (Maven, Swing)</p>
  </div>
</div>

## Các Dependency sử dụng
- JavaParser
  + Maven
    ```xml
        <dependency>
            <groupId>com.github.javaparser</groupId>
            <artifactId>javaparser-core</artifactId>
            <version>3.25.8</version>
        </dependency>
    ```
    </p>
  + Gradle
    ```xml
      implementation group: 'com.github.javaparser', name: 'javaparser-core', version: '3.25.8'
    ```
- OpenNLP (Bắt buộc: Phải download 2 file 2 bin: en-chunker.bin và en-pos-maxent.bin [tại đây](https://opennlp.sourceforge.net/models-1.5/))
  + Maven
    ```xml
        <dependency>
            <groupId>org.apache.opennlp</groupId>
            <artifactId>opennlp-tools</artifactId>
            <version>1.8.4</version>
        </dependency>
    ```
  + Gradle
    ```xml
      implementation group: 'org.apache.opennlp', name: 'opennlp-tools', version: '1.8.4'
    ```
## Demo Chương trình
- Project được sử dụng để chạy thử chương trình: [Click here](https://drive.google.com/drive/folders/1W0UuWDcKCRnQ5U4G4aGfJoQ-H-mWcwNy?usp=sharing)
- Hướng dẫn sử dụng 🚑
  + B1: Chạy chương trình, sẽ hiện thị giao diện như dưới đây:
    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/f4657a1b-952c-42e1-855c-f27f64d9fae3)
  + B2: Chọn project cần kiểm thử, và chọn file txt cần ghi vào (Nhấn vào choose):
    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/3999e215-b32b-4891-8886-42983e042ec0)

    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/5d68949a-7930-4b2c-addb-2557908bcb38)
  + B3: Click vào button Check, khi hoàn tất kiếm thử path của Choose project và Choose File output hiển thị trên giao diện sẽ bị xóa
    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/7ee82139-8f33-42d0-9325-585283672dc9)
  + B4: Kiểm tra file report:
    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/d728357a-be46-489a-a869-694602fbc373)

    ![image](https://github.com/nguyenhieu1435/week3_lab2_softwarearchitecture/assets/70377398/13bf2fef-0264-4e1b-ba90-f839b134f07c)
