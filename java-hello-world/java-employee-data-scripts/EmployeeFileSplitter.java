import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 従業員データを部署ごとに分割して社員番号昇順でCSVファイルとして出力するプログラム。
 * 数百万レコードにも対応できるように設計。
 * 
 * 処理の流れ：
 * 1. 入力ファイルを読み込み、従業員データをリストに格納。
 * 2. 社員番号順にデータをソート。
 * 3. 部署ごとに分けてCSVファイルとして出力。
 */
public class EmployeeFileSplitter {

    // 入力ファイルパスと出力ディレクトリ名の定義
    private static final String INPUT_FILE_PATH = "./employee.csv";  // カレントディレクトリのemployee.csv
    private static final String OUTPUT_DIR_NAME = "post";  // 出力先ディレクトリ名

    public static void main(String[] args) {
        // 出力ディレクトリパスを取得
        String outputDirPath = getOutputDirPath();
        
        // 出力ディレクトリの作成
        createOutputDirectory(outputDirPath);

        // 部署ごとのファイル書き出しストリームを管理するマップ
        Map<String, BufferedWriter> writers = new HashMap<>();
        
        // 全レコードを格納するリスト
        List<String> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(INPUT_FILE_PATH))) {
            // ヘッダー行の読み込み
            String header = readHeader(reader);
            if (header == null) return;  // 空ファイルの場合は処理を終了

            // レコード行の読み込みと格納
            processLines(reader, records);

            // 社員番号でソート
            records.sort(Comparator.comparing(record -> {
                String[] columns = record.split(",");
                return Integer.parseInt(columns[1].trim());  // 社員番号は2番目の列（インデックス1）
            }));

            // ソートされたレコードを部署ごとにファイルに書き込む
            writeSortedRecordsToFile(records, writers, header);

        } catch (IOException e) {
            System.err.println("ファイルの読み書き中にエラーが発生しました: " + e.getMessage());
        } finally {
            // 各ファイルを閉じる
            closeWriters(writers);
        }
    }

    /**
     * 出力ディレクトリのパスを取得する。
     * 
     * @return 出力ディレクトリのパス
     */
    private static String getOutputDirPath() {
        return Paths.get("").toAbsolutePath().resolve(OUTPUT_DIR_NAME).toString();
    }

    /**
     * 出力ディレクトリが存在しない場合は作成する。
     * 
     * @param outputDirPath 出力ディレクトリのパス
     */
    private static void createOutputDirectory(String outputDirPath) {
        try {
            Path outputDir = Paths.get(outputDirPath);
            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("出力ディレクトリ 'post' を作成しました。");
            }
        } catch (IOException e) {
            System.err.println("出力ディレクトリの作成中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * CSVファイルのヘッダー行を読み込む。
     * 
     * @param reader CSVファイルのBufferedReader
     * @return ヘッダー行、またはファイルが空の場合はnull
     * @throws IOException 入出力例外
     */
    private static String readHeader(BufferedReader reader) throws IOException {
        String header = reader.readLine();
        if (header == null) {
            System.out.println("employee.csvが空です。");
            return null;
        }
        return header;
    }

    /**
     * CSVの各行のデータを読み込み、リストに格納する。
     * 
     * @param reader CSVファイルのBufferedReader
     * @param records 読み込んだレコードを格納するリスト
     * @throws IOException 入出力例外
     */
    private static void processLines(BufferedReader reader, List<String> records) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(",");
            if (columns.length != 4) continue;  // データが欠けている行はスキップ
            records.add(line);  // 正常な行をリストに追加
        }
    }

    /**
     * ソートされたレコードを部署ごとにファイルに書き込む。
     * 
     * @param records ソートされたレコード
     * @param writers 部署ごとのBufferedWriterを管理するマップ
     * @param header CSVのヘッダー行
     * @throws IOException 入出力例外
     */
    private static void writeSortedRecordsToFile(List<String> records, Map<String, BufferedWriter> writers, String header) throws IOException {
        for (String record : records) {
            String[] columns = record.split(",");
            String department = columns[0].trim();  // 部署名は1番目の列（インデックス0）
            BufferedWriter writer = getWriterForDepartment(writers, department, header);
            
            // ソートされたデータ行を書き込む
            writer.write(record);
            writer.newLine();
            
            // 定期的にファイルをフラッシュしてメモリの使用量を抑える
            writer.flush();
        }
    }

    /**
     * 部署ごとにファイルを作成し、ファイル書き込みのためのBufferedWriterを取得する。
     * 既に作成されている場合はそのまま返却し、新たに作成する場合はファイルにヘッダーを追加する。
     * 
     * @param writers 部署ごとのBufferedWriterを管理するマップ
     * @param department 部署名
     * @param header CSVのヘッダー行
     * @return 部署用のBufferedWriter
     */
    private static BufferedWriter getWriterForDepartment(Map<String, BufferedWriter> writers, String department, String header) {
        return writers.computeIfAbsent(department, dept -> {
            try {
                // 部署名.csvという名前でファイルをpostディレクトリに作成
                BufferedWriter newWriter = Files.newBufferedWriter(
                        Paths.get(getOutputDirPath(), dept + ".csv")
                );
                newWriter.write(header);  // ヘッダーをファイルに書き込み
                newWriter.newLine();
                return newWriter;
            } catch (IOException e) {
                throw new UncheckedIOException(e);  // エラーが発生した場合はUncheckedIOExceptionとして再スロー
            }
        });
    }

    /**
     * 部署ごとに開かれたファイルを閉じる。
     * 
     * @param writers 開かれたファイルのBufferedWriterマップ
     */
    private static void closeWriters(Map<String, BufferedWriter> writers) {
        writers.values().forEach(writer -> {
            try {
                writer.close();  // ファイルを閉じる
            } catch (IOException e) {
                System.err.println("ファイルクローズ中にエラーが発生しました: " + e.getMessage());
            }
        });
    }
}
