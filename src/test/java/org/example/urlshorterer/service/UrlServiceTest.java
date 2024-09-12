package org.example.urlshorterer.service;

import org.example.urlshorterer.model.Url;
import org.example.urlshorterer.repo.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    private final String originalUrl = "http://example.com";
    private final String shortUrl = "abc123";
    private Url url;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Инициализация тестового объекта Url
        url = new Url();
        url.setId(1L);
        url.setOriginalUrl(originalUrl);
        url.setShortUrl(shortUrl);
    }

    @Test
    void testShortenUrl_WhenUrlAlreadyExists() {
        // Мокаем существующий URL
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(url));

        // Вызываем метод
        String result = urlService.shortenUrl(originalUrl);

        // Проверяем результат
        assertEquals("http://localhost:8080/" + shortUrl, result);

        // Убеждаемся, что метод save не вызывается
        verify(urlRepository, never()).save(any());
    }

    @Test
    void testShortenUrl_WhenUrlIsNew() {
        // Мокаем ситуацию, когда URL новый
        when(urlRepository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(urlRepository.findByShortUrl(anyString())).thenReturn(Optional.empty());

        // Вызываем метод
        String result = urlService.shortenUrl(originalUrl);

        // Убеждаемся, что URL сохраняется
        verify(urlRepository, times(1)).save(any());

        // Проверяем, что короткий URL начинается с базового URL
        assertTrue(result.startsWith("http://localhost:8080/"));
    }

    @Test
    void testGetOriginalUrl_WhenShortUrlExists() {
        // Мокаем существующий короткий URL
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(url));

        // Вызываем метод
        String result = urlService.getOriginalUrl(shortUrl);

        // Проверяем результат
        assertEquals(originalUrl, result);
    }

    @Test
    void testGetOriginalUrl_WhenShortUrlDoesNotExist() {
        // Мокаем ситуацию, когда короткий URL не существует
        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        // Вызываем метод
        String result = urlService.getOriginalUrl(shortUrl);

        // Проверяем результат (ожидается null)
        assertNull(result);
    }
}

