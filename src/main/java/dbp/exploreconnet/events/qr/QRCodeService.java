package dbp.exploreconnet.events.qr;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QRCodeService {

    private static final String QR_API_URL = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=";

    public String generateQRCodeUrl(Long id, String date, int numberOfPeople, String placeName, String userName) {
        ReservationData data = new ReservationData(id, date, numberOfPeople, placeName, userName);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(data);

            String encodedData = URLEncoder.encode(jsonData, StandardCharsets.UTF_8.toString());

            return QR_API_URL + encodedData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class ReservationData {
        public Long id;
        public String date;
        public int numberOfPeople;
        public String placeName;
        public String userName;

        public ReservationData(Long id, String date, int numberOfPeople, String placeName, String userName) {
            this.id = id;
            this.date = date;
            this.numberOfPeople = numberOfPeople;
            this.placeName = placeName;
            this.userName = userName;


        }
    }
}
