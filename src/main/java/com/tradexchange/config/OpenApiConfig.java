package com.tradexchange.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tradingPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradeXchange Trading Platform API")
                        .description("High-performance event-driven trading engine backend with real-time order matching, depth book, trade execution, OHLC candles, and WebSocket streaming.")
                        .version("1.0.0")
                        .contact(new Contact().name("TradeXchange Engineering Team"))
                        .license(new License().name("ISC").url("https://opensource.org/licenses/ISC")));
    }
}
