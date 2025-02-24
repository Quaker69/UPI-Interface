package org.example;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final String VPA = "9901529618@ybl"; // Replace with actual UPI ID
    private static final String PAYEE_NAME = "Shashidhar"; // Replace with your name
    private static final String CURRENCY = "INR"; // Currency format

    public static void main(String[] args) {
        // Show option dialog
        String[] options = {"Generate QR Code", "Scan QR Code"};
        int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "UPI QR Code App",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            generateUPIQRCode();
        } else if (choice == 1) {
            scanQRCode();
        }
    }

    // Method to generate UPI QR Code
    private static void generateUPIQRCode() {
        String amount = JOptionPane.showInputDialog("Enter amount:");

        if (amount == null || amount.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Amount cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Generate UPI QR Code String
            String upiLink = "upi://pay?pa=" + VPA + "&pn=" + PAYEE_NAME + "&am=" + amount + "&cu=" + CURRENCY;
            System.out.println("Generated UPI Link: " + upiLink);

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(upiLink, BarcodeFormat.QR_CODE, 300, 300);

            // Save as Image
            String filePath = "upi_qr.png";
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", Paths.get(filePath));
            System.out.println("UPI QR Code saved: " + filePath);

            // Show QR Code in GUI
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            showImage(qrImage, "Generated UPI QR Code");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating QR Code!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to scan QR Code
    private static void scanQRCode() {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(null, "No webcam detected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();
        QRCodeReader qrCodeReader = new QRCodeReader();

        JOptionPane.showMessageDialog(null, "Position the QR Code in front of the camera.");

        while (true) {
            BufferedImage image = webcam.getImage();

            // Convert to BinaryBitmap for ZXing
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                // Decode QR Code
                Result result = qrCodeReader.decode(bitmap);
                String qrText = result.getText();
                System.out.println("Scanned QR Code: " + qrText);

                if (qrText.startsWith("upi://pay")) {
                    // Extract UPI Payment Details
                    Map<String, String> upiData = parseUPIQRCode(qrText);
                    String message = "Payee: " + upiData.get("pn") + "\nVPA: " + upiData.get("pa") +
                            "\nAmount: ₹" + upiData.get("am");

                    JOptionPane.showMessageDialog(null, message, "UPI Payment Details", JOptionPane.INFORMATION_MESSAGE);
                }

                break; // Stop scanning after successful scan
            } catch (NotFoundException | ChecksumException | FormatException e) {
                // No QR code found, continue scanning
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        webcam.close();
    }

    // Method to parse scanned UPI QR code
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

    // Method to display an image in a JFrame
    private static void showImage(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 400);

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);

        frame.setVisible(true);
    }
}
