package org.example;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String VPA = "6363434649@ybl";
    private static final String PAYEE_NAME = "Shashidhar";
    private static final String CURRENCY = "INR";

    public static void main(String[] args) {
        String[] options = {"Generate QR Code", "Scan QR Code"};
        int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "UPI QR Code App",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            generateUPIQRCode();
        } else if (choice == 1) {
            scanQRCode();
        }
    }

    private static void generateUPIQRCode() {
        String amount = JOptionPane.showInputDialog("Enter amount:");

        if (amount == null || amount.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Amount cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String upiLink = "upi://pay?pa=" + VPA + "&pn=" + PAYEE_NAME + "&am=" + amount + "&cu=" + CURRENCY;
            System.out.println("Generated UPI Link: " + upiLink);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(upiLink, BarcodeFormat.QR_CODE, 300, 300);

            String filePath = "upi_qr.png";
            JOptionPane.showMessageDialog(null, "QR Code saved as " + filePath);
            BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    qrImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            showImage(qrImage, "Generated UPI QR Code");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating QR Code!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void scanQRCode() {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(null, "No webcam detected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();
        QRCodeReader qrCodeReader = new QRCodeReader();

        JFrame frame = new JFrame("QR Code Scanner");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(640, 480);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);

                    int overlaySize = 200;
                    int x = (this.getWidth() - overlaySize) / 2;
                    int y = (this.getHeight() - overlaySize) / 2;
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, overlaySize, overlaySize);
                }
            }
        };

        frame.add(panel);
        frame.setVisible(true);

        new Timer(100, e -> panel.repaint()).start();

        while (true) {
            BufferedImage image = webcam.getImage();
            if (image == null) continue;

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                Result result = qrCodeReader.decode(bitmap);
                String qrText = result.getText();
                System.out.println("Scanned QR Code: " + qrText);

                if (qrText.startsWith("upi://pay")) {
                    Map<String, String> upiData = parseUPIQRCode(qrText);
                    String message = "Payee: " + upiData.get("pn") + "\nVPA: " + upiData.get("pa") +
                            "\nAmount: ₹" + upiData.get("am");
                    JOptionPane.showMessageDialog(frame, message, "UPI Payment Details", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            } catch (NotFoundException | ChecksumException | FormatException ignored) {
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

        webcam.close();
        frame.dispose();
    }

    private static Map<String, String> parseUPIQRCode(String qrData) {
        Map<String, String> upiData = new HashMap<>();
        String[] parts = qrData.split("&");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                upiData.put(keyValue[0].replace("upi://pay?", ""), keyValue[1]);
            }
        }
        return upiData;
    }

    private static void showImage(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 400);
        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.setVisible(true);
    }
}
