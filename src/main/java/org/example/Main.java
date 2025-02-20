package org.example;






import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.image.BufferedImage;
import java.io.IOException;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        // Initialize the webcam
        Webcam webcam = null;
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                System.out.println("No webcam detected");
                return;
            }

            // Set resolution
            webcam.setViewSize(WebcamResolution.VGA.getSize());

            // Open the webcam
            webcam.open();

            // QR Code decoder
            QRCodeReader qrCodeReader = new QRCodeReader();

            System.out.println("Starting webcam to scan QR Code...");

            // Continuously capture frames from the webcam
            while (true) {
                BufferedImage image = webcam.getImage();

                // Convert BufferedImage to LuminanceSource for ZXing
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    // Decode the QR code from the frame
                    Result result = qrCodeReader.decode(bitmap);

                    // Print the decoded QR code content
                    System.out.println("Decoded QR Code: " + result.getText());

                    // Check if the QR code contains UPI details
                    String qrData = result.getText();
                    if (qrData.startsWith("upi://pay")) {
                        String[] dataParts = qrData.split("&");
                        for (String part : dataParts) {
                            if (part.startsWith("pa=")) {
                                System.out.println("Payee VPA (pa): " + part.substring(3));
                            } else if (part.startsWith("pn=")) {
                                System.out.println("Payee Name (pn): " + part.substring(3));
                            } else if (part.startsWith("am=")) {
                                System.out.println("Amount (am): " + part.substring(3));
                            }
                        }

                        // Capture and display the image when QR code is detected
                        saveAndDisplayImage(image);
                    }

                } catch (NotFoundException | ChecksumException | FormatException e) {
                    // No QR code found or error decoding, just continue
                    // System.out.println("No QR code found.");
                }

                // Add a small delay to avoid overloading the CPU
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (WebcamException e) {
            e.printStackTrace();
        } finally {
            if (webcam != null) {
                webcam.close();
            }
        }
    }

    // Method to save and display the captured image
    private static void saveAndDisplayImage(BufferedImage image) {
        try {
            // Save the image to a file
            File outputfile = new File("captured_qr_image.png");
            javax.imageio.ImageIO.write(image, "PNG", outputfile);
            System.out.println("Image saved to: " + outputfile.getAbsolutePath());

            // Display the image in a window
            displayImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to display the captured image in a JFrame
    private static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame("Captured QR Code Image");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);

        // Create a label to hold the image
        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label, BorderLayout.CENTER);

        // Display the window
        frame.setVisible(true);
    }
}
