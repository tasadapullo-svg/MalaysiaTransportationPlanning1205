
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
@Deprecated
public class HereApiTest {

    public static void main(String[] args) {

        // 用你的新 key
        String apiKey = "utkqloI0sXV-H7xazoluAcxN_61roaMx2OCJuN49f8k";

//        https://data.traffic.hereapi.com/v7/flow?in=bbox:1.20,109.60,2.05,111.00&apiKey=utkqloI0sXV-H7xazoluAcxN_61roaMx2OCJuN49f8k


        String bbox = "1.20,109.60;2.05,111.00";

        String urlString =
                "https://traffic.ls.hereapi.com/traffic/6.3/flow.json"
                        + "?apiKey=" + apiKey
                        + "&bbox=" + bbox;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            System.out.println("HTTP Code=" + code);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    code >= 400 ? conn.getErrorStream() : conn.getInputStream()
            ));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) sb.append(line).append("\n");
            in.close();

            System.out.println("Response:");
            System.out.println(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
