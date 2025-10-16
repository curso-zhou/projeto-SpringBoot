package br.pucpr.projeto.livros.service;

import br.pucpr.projeto.livros.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;

import org.springframework.web.client.RestClient;
 

@Service
public class PriceService {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    

    private final LivroRepository livros;
    private final RestClient googleBooks = RestClient.builder().baseUrl("https://www.googleapis.com/books/v1").build();

    public PriceService(LivroRepository livros) {
        this.livros = livros;
    }

    // Tenta obter preço do catálogo local (se existir) como fallback rápido.
    public Optional<BigDecimal> getAmazonBrPrice(String isbn) {
        if (isbn == null || isbn.isBlank()) return Optional.empty();
        var sanitized = isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
        // 1) Se já existe no catálogo com preço > 0, reutiliza
        var local = livros.findByIsbn(sanitized)
                .map(l -> l.getPreco() != null && l.getPreco().compareTo(BigDecimal.ZERO) > 0 ? l.getPreco() : null)
                .map(Optional::ofNullable)
                .orElseGet(Optional::empty);

        if (local.isPresent()) return local;

        // 2) Tenta Amazon se credenciais estiverem configuradas (PA-API)
        // 2) Fallback: tenta Google Books (apenas se currency for BRL)
        return tryGoogleBooksPriceBRL(sanitized);
    }

    public String formatBR(BigDecimal value) {
        if (value == null) return null;
        var fmt = NumberFormat.getCurrencyInstance(PT_BR);
        return fmt.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private Optional<BigDecimal> tryGoogleBooksPriceBRL(String isbn) {
        try {
            var bodyOpt = getGoogleVolumeBody(isbn);
            if (bodyOpt.isEmpty()) return Optional.empty();
            var firstMapOpt = getFirstItemAsMap(bodyOpt.get().get("items"));
            if (firstMapOpt.isEmpty()) return Optional.empty();
            var saleInfoOpt = getMap(firstMapOpt.get().get("saleInfo"));
            if (saleInfoOpt.isEmpty()) return Optional.empty();

            var price = extractPriceFromSaleInfo(saleInfoOpt.get());
            return price.filter(p -> p.currency != null && "BRL".equalsIgnoreCase(p.currency))
                    .map(p -> p.amount);
        } catch (RuntimeException _) {
            return Optional.empty();
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        try {
            if (value instanceof Number n) return new BigDecimal(n.toString());
            return new BigDecimal(value.toString());
        } catch (RuntimeException _) {
            return null;
        }
    }

    private Optional<Map<?,?>> getGoogleVolumeBody(String isbn) {
        var response = googleBooks.get()
                .uri(uri -> uri.path("/volumes").queryParam("q", "isbn:" + isbn).build())
                .retrieve()
                .toEntity(Map.class);
        return Optional.ofNullable(response.getBody());
    }

    private Optional<Map<?,?>> getFirstItemAsMap(Object itemsObj) {
        if (!(itemsObj instanceof java.util.List<?> items) || items.isEmpty()) return Optional.empty();
        var first = items.get(0);
        if (!(first instanceof Map<?,?> firstMap)) return Optional.empty();
        return Optional.of(firstMap);
    }

    private Optional<Map<?,?>> getMap(Object obj) {
        return obj instanceof Map<?,?> m ? Optional.of(m) : Optional.empty();
    }

    private record Price(BigDecimal amount, String currency) { }

    private Optional<Price> extractPriceFromSaleInfo(Map<?,?> saleInfo) {
        var p1 = getMap(saleInfo.get("listPrice")).flatMap(this::parsePrice);
        if (p1.isPresent()) return p1;
        return getMap(saleInfo.get("retailPrice")).flatMap(this::parsePrice);
    }

    private Optional<Price> parsePrice(Map<?,?> priceMap) {
        if (priceMap == null) return Optional.empty();
        var a = priceMap.get("amount");
        var c = priceMap.get("currencyCode");
        var amount = a != null ? toBigDecimal(a) : null;
        var currency = c != null ? c.toString() : null;
        if (amount == null || currency == null) return Optional.empty();
        return Optional.of(new Price(amount, currency));
    }
}
