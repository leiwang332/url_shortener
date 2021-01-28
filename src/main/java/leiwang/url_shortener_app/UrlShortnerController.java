package leiwang.url_shortener_app;

import com.google.common.hash.Hashing;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(value = "/short_url")
public class UrlShortnerController {
    @Autowired
    private StringRedisTemplate redis;

    // Translate the short URL to long URL
    @GetMapping(value = "/{id}")
    public void translate(@PathVariable String id, HttpServletResponse resp)
            throws Exception {
        // Get the long URL corresponding to the short url (represented by id)
        final String url = redis.opsForValue().get(id);
        if (url != null)
            resp.sendRedirect(url);
        else
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Create a short URL for a long URL
    @PostMapping
    public ResponseEntity<String> create(@RequestBody final String url) {
        final UrlValidator urlValidator = new UrlValidator(new String[]{"http"
                , "https"});
        String actualUrl = URLDecoder.decode(url.split("=")[1],
                StandardCharsets.UTF_8);
        if (urlValidator.isValid(actualUrl)) {
            // Generate a hash as the short URL
            final String id =
                    Hashing.murmur3_32(
                            (int) System.currentTimeMillis()).hashString(
                                    actualUrl, StandardCharsets.UTF_8)
                            .toString();
            redis.opsForValue().set(id, actualUrl);
            // For demo purpose, return localhost as the domain since we
            // are not really hosting this as a real site on the web.
            return new ResponseEntity<>("http://localhost:12121/short_url/" + id,
                    HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
