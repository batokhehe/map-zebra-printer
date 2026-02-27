package com.werhoz.mapzebraprinter.utils;

import com.werhoz.mapzebraprinter.data.model.ResultModel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class TemplateGenerator {

    private static int MAX_CHARS_PER_LINE = 21;

    public static String generatePriceHeader(String content, String price, String header) {
        int boxWidth = 264;
        int fontWidthEstimate = price.length() * 12;
        int x1 = 10;
        int gap = 300;

        //section 1
        int x1_1 = x1 + ((boxWidth - fontWidthEstimate) / 2);
        int y1_1 = 5;
        content = content.replace("{x1_1}", String.valueOf(x1_1));
        content = content.replace("{y1_1}", String.valueOf(y1_1));
        content = content.replace("{header_1}", header);

        int x2_1 = x1_1;
        int y2_1 = y1_1 + 40;
        content = content.replace("{x2_1}", String.valueOf(x2_1));
        content = content.replace("{y2_1}", String.valueOf(y2_1));
        content = content.replace("{price_1}", price);

        // section 2
        int x1_2 = x1_1 + gap;
        int y1_2 = y1_1;
        content = content.replace("{x1_2}", String.valueOf(x1_2));
        content = content.replace("{y1_2}", String.valueOf(y1_2));
        content = content.replace("{header_2}", header);

        int x2_2 = x1_2;
        int y2_2 = y2_1;
        content = content.replace("{x2_2}", String.valueOf(x2_2));
        content = content.replace("{y2_2}", String.valueOf(y2_2));
        content = content.replace("{price_2}", price);

        return content;
    }

    public static String generatePriceSale(String content, String price) {
        int boxWidth = 244;
        int fontWidthEstimate = price.length() * 12;
        int x1 = 40;
        int gap = 300;

        //section 1
        int x1_1 = x1 + ((boxWidth - fontWidthEstimate) / 2);
        int y1_1 = 30;
        content = content.replace("{x1_1}", String.valueOf(x1_1));
        content = content.replace("{y1_1}", String.valueOf(y1_1));
        content = content.replace("{price_1}", price);

        // section 2
        int x2_1 = x1_1 + gap;
        int y2_1 = 30;
        content = content.replace("{x2_1}", String.valueOf(x2_1));
        content = content.replace("{y2_1}", String.valueOf(y2_1));
        content = content.replace("{price_2}", price);

        return content;
    }

    public static String generatePriceSaleVertical(String content, String price, String header) {
        int boxWidth = 244;
        int fontWidthEstimate = price.length() * 12;
        int x1 = 43;
        int gap = 300;

        //section 1
        int x1_1 = x1 + ((boxWidth + 20 - fontWidthEstimate) / 2);
        int y1_1 = 30;
        content = content.replace("{x1_1}", String.valueOf(x1_1));
        content = content.replace("{y1_1}", String.valueOf(y1_1));
        content = content.replace("{price_1}", price);

        int x2_1 = 55;
        int y2_1 = 80;
        content = content.replace("{x2_1}", String.valueOf(x2_1));
        content = content.replace("{y2_1}", String.valueOf(y2_1));
        content = content.replace("{header_1}", header);

        int x3_1 = x1 + 40;
        int x4_1 = x1 + 40;
        content = content.replace("{x3_1}", String.valueOf(x3_1));
        content = content.replace("{x4_1}", String.valueOf(x4_1));

        // section 2
        int x1_2 = x1_1 + gap;
        int y1_2 = 30;
        content = content.replace("{x1_2}", String.valueOf(x1_2));
        content = content.replace("{y1_2}", String.valueOf(y1_2));
        content = content.replace("{price_2}", price);

        int x2_2 = x2_1 + gap;
        int y2_2 = y2_1;
        content = content.replace("{x2_2}", String.valueOf(x2_2));
        content = content.replace("{y2_2}", String.valueOf(y2_2));
        content = content.replace("{header_2}", header);

        int x3_2 = x3_1 + gap;
        int x4_2 = x4_1 + gap;
        content = content.replace("{x3_2}", String.valueOf(x3_2));
        content = content.replace("{x4_2}", String.valueOf(x4_2));

        return content;
    }

    public static String generatePriceVertical(String content, String price, String header) {
        int boxWidth = 244;
        int fontWidthEstimate = price.length() * 12;
        int x1 = 10;
        int gap = 300;

        //section 1
        int x1_1 = x1 + ((boxWidth + 20 - fontWidthEstimate) / 2);
        int y1_1 = 30;
        content = content.replace("{x1_1}", String.valueOf(x1_1));
        content = content.replace("{y1_1}", String.valueOf(y1_1));
        content = content.replace("{price_1}", price);

        int x2_1 = x1 + 5;
        int y2_1 = 80;
        content = content.replace("{x2_1}", String.valueOf(x2_1));
        content = content.replace("{y2_1}", String.valueOf(y2_1));
        content = content.replace("{header_1}", header);

        int x3_1 = x1 + 35;
        int x4_1 = x1 + 35;
        content = content.replace("{x3_1}", String.valueOf(x3_1));
        content = content.replace("{x4_1}", String.valueOf(x4_1));

        // section 2
        int x1_2 = x1_1 + gap;
        int y1_2 = 30;
        content = content.replace("{x1_2}", String.valueOf(x1_2));
        content = content.replace("{y1_2}", String.valueOf(y1_2));
        content = content.replace("{price_2}", price);

        int x2_2 = x2_1 + gap;
        int y2_2 = y2_1;
        content = content.replace("{x2_2}", String.valueOf(x2_2));
        content = content.replace("{y2_2}", String.valueOf(y2_2));
        content = content.replace("{header_2}", header);

        int x3_2 = x3_1 + gap;
        int x4_2 = x4_1 + gap;
        content = content.replace("{x3_2}", String.valueOf(x3_2));
        content = content.replace("{x4_2}", String.valueOf(x4_2));

        return content;
    }

    public static String generateActive(String content, int qty, ResultModel itemResponse, String header) {
        int startX = 7;
        int boxWidth = 264;
        int gap = 300;

        StringBuilder text = new StringBuilder();

        double wasPrice = parseDouble(itemResponse.wasPrice);
        double nowPrice = parseDouble(itemResponse.currentPrice);

        int counter = (qty > 1) ? 2 : 1;

        for (int i = 0; i < counter; i++) {
            int col = i % 2;        // kolom kiri/kanan
            int x1 = startX + (col == 0 ? 0 : gap);

            // Garis dalam box
            text.append(String.format("L %d %d %d %d 1\n", x1 + 15, 46, x1 + 248, 46));
            text.append(String.format("L %d %d %d %d 1\n", x1 + 33, 146, x1 + 236, 146));
            text.append(String.format("L %d %d %d %d 1\n", x1 + 15, 190, x1 + 248, 190));
            text.append(String.format("L %d %d %d %d 1\n", x1 + 19, 340, x1 + 245, 340));
            text.append(String.format("L %d %d %d %d 1\n", x1 + 15, 435, x1 + 245, 435));

            // Variant/Single Article
            int variantX = x1 + ((boxWidth - (itemResponse.itemNumber.length() * 12)) / 2);
            text.append(String.format("T 7 0 %d %d %s\n", variantX, 6, itemResponse.itemNumber));

            // Description (wrap text)
            text.append(wrapText(itemResponse.description, 55, 15, startX, 0, x1, 15)).append("\n");

            // Category
            String category = itemResponse.productCategory;
            if (!header.isEmpty()) category = category + " - " + header;
            int categoryX = Math.max(1, (x1 + ((boxWidth - (category.length() * 12)) / 2)));
            text.append(String.format("T 7 0 %d %d %s\n", categoryX, 156, category));

            // WAS price
            if (wasPrice > nowPrice) {
                String priceWas = itemResponse.currency + " " + formatNumber(itemResponse.wasPrice, itemResponse.currency);
                text.append(String.format("T 0 2 %d %d WAS :  %s\n", x1 + 19, 351, priceWas));
                text.append(String.format("LINE %d %d %d %d 1\n", x1 + 19 + 50, 361, x1 + 19 + 50 + (priceWas.length() * 10), 361));
            }
            // NOW price
            text.append(String.format("T 0 2 %d %d NOW :  %s %s\n", x1 + 19, 381, itemResponse.currency, formatNumber(itemResponse.currentPrice, itemResponse.currency)));

            // Barcode
            int barcodeX = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 18)) / 2);
            text.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, 200, itemResponse.eANNumber));

            // Barcode Text
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 12)) / 2);
            text.append(String.format("T 5 0 %d %d %s\n", xText, 310, itemResponse.eANNumber));
        }

        return content.replace("{CONTENT}", text.toString());
    }

    public static String generateMango(String content, int qty, ResultModel itemResponse) {
        int startX = 7;
        int boxWidth = 264;
        int gap = 300;

        StringBuilder stringBuilder = new StringBuilder();

        double wasPrice = parseDouble(itemResponse.wasPrice);
        double nowPrice = parseDouble(itemResponse.currentPrice);

        int counter = (qty > 1) ? 2 : 1;

        for (int i = 0; i < counter; i++) {
            int col = i % 2;        // kolom kiri/kanan
            int x1 = startX + (col == 0 ? 0 : gap);

            // Variant/Article (item number)
            int variantX = x1 + ((boxWidth - (itemResponse.itemNumber.length() * 12)) / 2);
            stringBuilder.append(String.format("T 7 0 %d %d %s\n", variantX, 36, itemResponse.itemNumber));

            // Description (wrap text)
            int descX = x1 + 7;
            int index = 0;
            String text = itemResponse.description;
            int yText = 75;
            while (index < text.length()) {
                int end = Math.min(index + MAX_CHARS_PER_LINE, text.length());
                String line = text.substring(index, end);
                stringBuilder.append(String.format("T 7 0 %d %d %s\n", descX, yText, line));
                yText += 30;
                index += MAX_CHARS_PER_LINE;
            }

            // Price
            if (wasPrice > nowPrice) {
                String priceWas = "WAS: " + itemResponse.currency + " " + formatNumber(itemResponse.wasPrice, itemResponse.currency);
                stringBuilder.append(String.format("T 0 2 %d %d %s\n", x1 + 19, 310, priceWas));
                stringBuilder.append(String.format("LINE %d %d %d %d 1\n", x1 + 19 + 50, 320, x1 + (priceWas.length() * 10), 320));
            }
            String priceNow = "NOW: " + itemResponse.currency + " " + formatNumber(itemResponse.currentPrice, itemResponse.currency);
            stringBuilder.append(String.format("T 0 2 %d %d %s\n", x1 + 19, 345, priceNow));

            // Barcode
            int barcodeX = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 18)) / 2);
            stringBuilder.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, 150, itemResponse.eANNumber));

            // Barcode Text
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 12)) / 2);
            stringBuilder.append(String.format("T 5 0 %d %d %s\n", xText, 260, itemResponse.eANNumber));
        }

        return content.replace("{CONTENT}", stringBuilder.toString());
    }

    public static String generateAlo(int qty, ResultModel itemResponse) throws IOException {
        int startX = 7;
        int startY = 0;
        int boxWidth = 264;
        int boxHeight = 440;
        int columnSpacing = 32; // jarak antar kolom
        int rowSpacing = 27;    // jarak antar baris


        StringBuilder cpcl = new StringBuilder();

        for (int i = 0; i < qty; i++) {
            int col = i % 2;        // kiri/kanan
            int row = i / 2;  // baris ke-0/1 dalam 1 halaman

            int offsetX = col * (boxWidth + columnSpacing);
            int offsetY = row * (boxHeight + rowSpacing);

            int x1 = startX + offsetX;
            int y1 = startY + offsetY;
            int x2 = x1 + boxWidth;
            int y2 = y1 + boxHeight;

            // Titik acuan teks
            int xField = (col == 0) ? 10 : 305;
            int xColon = (col == 0) ? 100 : 395;

            // Print Title
            cpcl.append(String.format("T 5 2 %d %d alo\n", x1 + 105, y1 + 25));

//            cpcl.append("BITMAP ").append(x1 + 105).append(" ").append(y1 + 25).append(" ")
//                    .append(widthBytes).append(" ").append(height).append(" ")
//                    .append(dataLength).append("\n");

            // Print fields
            int xText = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 10)) / 2);
            cpcl.append(String.format("T 5 0 %d %d %s\n", xText, y1 + 205, itemResponse.eANNumber));
            cpcl.append(String.format("T 0 0 %d %d No. ARTIKEL\n", xField, y1 + 264));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 264, itemResponse.itemNumber));

            cpcl.append(String.format("T 0 0 %d %d UKURAN\n", xField, y1 + 285));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 285, itemResponse.size));

            cpcl.append(String.format("T 0 0 %d %d WARNA\n", xField, y1 + 304));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 304, itemResponse.color));

            cpcl.append(String.format("T 0 0 %d %d KATEGORI\n", xField, y1 + 324));
            cpcl.append(String.format("T 0 0 %d %d :%s\n", xColon, y1 + 324, itemResponse.productCategory));

            // Harga
            String price = itemResponse.currency + " " + formatNumber(itemResponse.currentPrice, itemResponse.currency);
//            int priceX = xField + ((boxWidth - (price.length() * 15)) / 2);
            cpcl.append(String.format("T 5 0 %d %d %s\n", x1 + 30, y1 + 379, price));

            // BARCODE
            int barcodeX = x1 + ((boxWidth - (itemResponse.eANNumber.length() * 18)) / 2);
            int barcodeY = y1 + 85;
            cpcl.append(String.format("BARCODE 128 1 1 100 %d %d %s\n", barcodeX, barcodeY, itemResponse.eANNumber));
        }

        return cpcl.toString();
    }

    public static Double parseDouble(String number) {
        return Double.parseDouble(number.replace(",", "."));
    }

    public static String wrapText(String text, int startYPos, int startXPos, int startX, int startY, int offsetX, int offsetY) {
        StringBuilder wrapped = new StringBuilder();
        int y = startY + offsetY + startYPos; // starting Y position for the first line
        int index = 0;

        while (index < text.length()) {
            int end = Math.min(index + MAX_CHARS_PER_LINE, text.length());
            String line = text.substring(index, end);
            wrapped.append("T 7 0 ")
                    .append(startX + startXPos + offsetX).append(" ")
                    .append(y).append(" ")
                    .append(line).append("\n");
            y += 30;
            index += MAX_CHARS_PER_LINE;
        }

        return wrapped.toString();
    }

    public static String formatNumber(String number, String currency) {

        double value = Double.parseDouble(number.replace(",", "."));

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter;

        if ("IDR".equalsIgnoreCase(currency)) {
            // Tanpa desimal
            formatter = new DecimalFormat("#,###", symbols);
        } else {
            // Dengan 2 angka desimal
            formatter = new DecimalFormat("#,##0.00", symbols);
        }

        return formatter.format(value);
    }
}
