import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * サンプルの従業員データを生成し、CSV形式でファイルに書き込むプログラム。
 * このプログラムは、指定された数の従業員データをランダムに生成し、部署、社員番号、役職、氏名を含むCSVファイルを作成します。
 * 
 * 処理の流れ：
 * 1. 部署、役職、苗字、名前のサンプルリストを準備。
 * 2. ユーザーから出力するレコード数を取得。
 * 3. 指定されたレコード数に基づいて、各フィールドをランダムに選択してCSV行を生成。
 * 4. 生成されたデータを"employee.csv"というファイルに出力。
 */
public class SampleEmployeeCsvGenerator {

    public static void main(String[] args) {
        // 出力ファイルのパスを定義
        String outputFilePath = "./employee.csv";

        // 部署名のサンプルリスト
        List<String> departments = Arrays.asList("営業部", "人事部", "経理部", "開発部");
        
        // 役職のサンプルリスト
        List<String> positions = Arrays.asList("部長", "課長", "一般職");

        // 苗字と名前のサンプルリスト
        List<String> surnames = Arrays.asList(
            "徳川", "織田", "豊臣", "武田", "伊達", "真田", "上杉", "明智", "石田", "前田",
            "坂本", "高杉", "吉田", "西郷", "大久保", "勝", "福沢", "佐久間", "近藤", "沖田",
            "渋沢", "井伊", "毛利", "島津", "伊東", "高橋", "後藤", "岡倉", "犬養", "岩倉",
            "松本", "大村", "吉村", "大塩", "新撰組", "山本", "東条", "小松", "横井", "田中",
            "鈴木", "佐藤", "小林", "加藤", "山田", "中村", "林", "長谷川", "石井", "木村"
        );

        List<String> givenNames = Arrays.asList(
            "家康", "信長", "秀吉", "信玄", "政宗", "幸村", "謙信", "光秀", "三成", "利家",
            "龍馬", "晋作", "松陰", "隆盛", "利通", "海舟", "諭吉", "象山", "勇", "総司",
            "栄一", "直弼", "元就", "義弘", "甲子太郎", "是清", "新平", "天心", "毅", "具視",
            "良順", "益次郎", "寅太郎", "平八郎", "歳三", "五十六", "英機", "実篤", "小楠", "正造",
            "一郎", "太郎", "次郎", "清", "健一", "浩", "修", "勇作", "俊介", "康平"
        );

        Random random = new Random(); // ランダムな値を生成するためのRandomオブジェクト

        // ユーザーから出力するレコード数を入力させる
        Scanner scanner = new Scanner(System.in);
        System.out.print("出力するレコード数を入力してください: ");
        int recordCount = scanner.nextInt(); // ユーザーが指定したレコード数を取得
        scanner.close(); // スキャナーを閉じる

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            // CSVのヘッダー行を書き込む
            writer.write("部署名,社員番号,役職,氏名");
            writer.write("\r\n");  // 改行コード（CRLF）

            // 指定されたレコード数分だけデータを生成して書き込む
            for (int i = 1; i <= recordCount; i++) {
                // 部署名をランダムに選択
                String department = departments.get(random.nextInt(departments.size()));
                
                // 社員番号を0埋めで4桁の形式にする
                String employeeId = String.format("%04d", i);
                
                // 役職をランダムに選択
                String position = positions.get(random.nextInt(positions.size()));

                // 苗字と名前をランダムに組み合わせる
                String surname = surnames.get(random.nextInt(surnames.size()));
                String givenName = givenNames.get(random.nextInt(givenNames.size()));
                String name = surname + " " + givenName;

                // CSV形式の行を作成
                String line = String.join(",", department, employeeId, position, name);

                // 作成した行をファイルに書き込む
                writer.write(line);
                writer.write("\r\n");  // 改行コード（CRLF）
            }

            // 正常にファイルが生成されたことを通知
            System.out.println("employee.csvが正常に生成されました。");
        } catch (IOException e) {
            // エラーメッセージを表示
            System.err.println("ファイル生成中にエラーが発生しました: " + e.getMessage());
        }
    }
}
