package br.pucpr.projeto.livros.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class ExternalBookLookupService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExternalBookLookupService.class);

    private final RestClient googleBooks = RestClient.builder().baseUrl("https://www.googleapis.com/books/v1").build();
    private final RestClient openLibrary = RestClient.builder().baseUrl("https://openlibrary.org").build();

    public BookInfo lookupByIsbn(String rawIsbn) {
        String isbn = sanitizeIsbn(rawIsbn);

        BookInfo info = fromGoogleBooks(isbn);
        if (info != null) return info;

        info = fromOpenLibrary(isbn);
        if (info != null) return info;

        info = fromOpenLibrarySearch(isbn);
        return info;
    }

    private BookInfo fromGoogleBooks(String isbn) {
        try {
            var response = googleBooks.get()
                    .uri(uri -> uri.path("/volumes").queryParam("q", "isbn:" + isbn).build())
                    .retrieve()
                    .toEntity(Map.class);

            var body = response.getBody();
            if (body == null || !body.containsKey("items")) return null;
            var itemsObj = body.get("items");
            if (!(itemsObj instanceof java.util.List<?> items) || items.isEmpty()) return null;
            var first = items.get(0);
            if (!(first instanceof Map<?,?> firstMap)) return null;
            var volumeObj = firstMap.get("volumeInfo");
            if (!(volumeObj instanceof Map<?,?> vol)) return null;

            Object titleObj = vol.get("title");
            String title = titleObj == null ? "" : titleObj.toString();
            String authors = null;
            var authorsObj = vol.get("authors");
            if (authorsObj instanceof java.util.List<?> list && !list.isEmpty()) {
                authors = String.join(", ", list.stream().map(Object::toString).toList());
            }
            String publisher = (String) vol.get("publisher");
            String description = (String) vol.get("description");
            Integer year = parseYear(vol.get("publishedDate"));

            String cover = null;
            var imgObj = vol.get("imageLinks");
            if (imgObj instanceof Map<?,?> img) {
                var thumb = img.get("thumbnail");
                if (thumb != null) cover = thumb.toString();
            }

            return new BookInfo(isbn, title, authors, publisher, cover, description, year);
        } catch (Exception ex) { // Ignora erros e tenta fallback
            log.debug("Falha ao consultar Google Books para ISBN {}", isbn, ex);
            return null;
        }
    }

    private BookInfo fromOpenLibrary(String isbn) {
        try {
            var response = openLibrary.get()
                    .uri(uri -> uri.path("/isbn/" + isbn + ".json").build())
                    .retrieve()
                    .toEntity(Map.class);
            var body = response.getBody();
            if (body == null) return null;
            String title = (String) body.get("title");
            String publishers = null;
            var pubsObj = body.get("publishers");
            if (pubsObj instanceof java.util.List<?> pubs && !pubs.isEmpty()) publishers = pubs.get(0).toString();
            Integer year = parseYear(body.get("publish_date"));
            return new BookInfo(isbn, title, null, publishers, null, null, year);
        } catch (Exception ex) { // Ignora erros da OpenLibrary
            log.debug("Falha ao consultar OpenLibrary para ISBN {}", isbn, ex);
            return null;
        }
    }

    private BookInfo fromOpenLibrarySearch(String isbn) {
        try {
            var response = openLibrary.get()
                    .uri(uri -> uri.path("/search.json").queryParam("isbn", isbn).build())
                    .retrieve()
                    .toEntity(Map.class);
            var body = response.getBody();
            if (body == null) return null;
            var docsObj = body.get("docs");
            if (!(docsObj instanceof java.util.List<?> docs) || docs.isEmpty()) return null;
            var first = docs.get(0);
            if (!(first instanceof Map<?,?> m)) return null;
            String title = m.get("title") != null ? m.get("title").toString() : null;
            String authors = null;
            var a = m.get("author_name");
            if (a instanceof java.util.List<?> list && !list.isEmpty()) authors = String.join(", ", list.stream().map(Object::toString).toList());
            String publisher = null;
            var p = m.get("publisher");
            if (p instanceof java.util.List<?> list2 && !list2.isEmpty()) publisher = list2.get(0).toString();
            Integer year = null;
            var pd = m.get("publish_date");
            if (pd instanceof java.util.List<?> list3 && !list3.isEmpty()) year = parseYear(list3.get(0));
            return new BookInfo(isbn, title, authors, publisher, null, null, year);
        } catch (Exception ex) {
            log.debug("Falha ao consultar OpenLibrary search para ISBN {}", isbn, ex);
            return null;
        }
    }

    private Integer parseYear(Object dateObj) {
        if (dateObj == null) return null;
        var s = dateObj.toString();
        // Procura a primeira sequência de 4 dígitos
        var m = java.util.regex.Pattern.compile("(\\d{4})").matcher(s);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (NumberFormatException ex) { log.trace("Data de publicação não numérica: {}", s, ex); }
        }
        return null;
    }

    private String sanitizeIsbn(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
    }

    public record BookInfo(String isbn, String titulo, String autor, String editora, String capa,
                           String descricao, Integer anoPublicacao) {}
}
