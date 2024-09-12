package org.example.urlshorterer.service;

import org.example.urlshorterer.model.Url;
import org.example.urlshorterer.repo.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UrlService {

    private static final String BASE_URL = "http://localhost:8080/";
    private static final int SHORT_URL_LENGTH = 6;

    @Autowired
    private UrlRepository urlRepository;

    public String shortenUrl(String originalUrl) {
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return BASE_URL + existingUrl.get().getShortUrl();
        }

        // Генерируем хеш для URL и берем первые SHORT_URL_LENGTH символов
        String shortUrl = generateShortUrl(originalUrl);

        // Проверяем коллизию и создаем новый хеш, если такая строка уже существует
        while (urlRepository.findByShortUrl(shortUrl).isPresent()) {
            originalUrl += System.nanoTime(); // Добавляем текущий таймштамп для изменения хеша
            shortUrl = generateShortUrl(originalUrl);
        }

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setShortUrl(shortUrl);
        urlRepository.save(url);

        return BASE_URL + shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {
        Optional<Url> url = urlRepository.findByShortUrl(shortUrl);
        return url.map(Url::getOriginalUrl).orElse(null);
    }

    private String generateShortUrl(String originalUrl) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(originalUrl.getBytes());
            BigInteger number = new BigInteger(1, hash);

            // Преобразуем хеш в строку в шестнадцатеричном формате и берем первые 6 символов
            StringBuilder shortUrl = new StringBuilder(number.toString(16));
            while (shortUrl.length() < SHORT_URL_LENGTH) {
                shortUrl.append("0"); // Добавляем ведущие нули, если длина хеша меньше 6 символов
            }

            return shortUrl.substring(0, SHORT_URL_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка при генерации хеша", e);
        }
    }
}
