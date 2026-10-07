package com.example.bookonlineapp.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "pdhtejei",
                "api_key", "543148714818317",
                "api_secret", "u3OQhX-C9sVuckz8fUHYb8K9VlU",
                "secure", true
        ));
    }
}