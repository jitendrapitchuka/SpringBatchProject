package com.jitendra.SpringBatchExample.Config;

import org.springframework.batch.item.ItemProcessor;

import com.jitendra.SpringBatchExample.Model.Product;

public class CustomItemProcessor implements ItemProcessor<Product,Product>{

    @Override
    public Product process(Product item) throws Exception {
        
        try {
            int discountPercent = Integer.parseInt(item.getDiscount().trim());
            double price = Double.parseDouble(item.getPrice().trim());
            double discountAmount = (price * discountPercent) / 100;
            double finalPrice = price - discountAmount;
            item.setDiscountedPrice(String.valueOf(finalPrice));
        } catch (Exception e) {
            System.err.println("Error processing item with Product ID: " + item.getProductId());
            e.printStackTrace();
        }
        return item;
    }

}
