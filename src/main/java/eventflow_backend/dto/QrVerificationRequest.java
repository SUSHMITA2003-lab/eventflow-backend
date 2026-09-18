package eventflow_backend.dto;

public class QrVerificationRequest {

    private String qrToken;

    public QrVerificationRequest() {
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
