package com.hotel.hotel_stars.Config;


import com.hotel.hotel_stars.Entity.Account;
import com.hotel.hotel_stars.Entity.Booking;
import com.hotel.hotel_stars.Repository.AccountRepository;
import com.hotel.hotel_stars.Repository.BookingRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtService {
     @Autowired
     AccountRepository userRepository;
     @Autowired
     BookingRepository bookingRepository;

 /*   public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";*/
   public static final String SECRET = "MjkVhpvE/QmeNruBwE+NSUnIN7kgz7+udKKVah6L5CQ=";
    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        Optional<Account> user=userRepository.findByUsername(userName);

        user.ifPresent(u -> {
            claims.put("id",u.getId());
            claims.put("username",u.getUsername());
            claims.put("rle",u.getRole().getRoleName());
            claims.put("phoone",u.getPhone());
            claims.put("email",u.getEmail());
            claims.put("avatar",u.getAvatar());
            claims.put("gender",u.getGender());
            claims.put("fullname",u.getFullname());
            System.out.println(u.getRole().getRoleName()+"111");
        });
        System.out.println("thời gian hiện tại: "+new Date(System.currentTimeMillis()));
        System.out.println("thời gian hết hạn: "+new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6 ));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.get().getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6 ))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractPassword(String token) {
        return extractClaim(token, claims -> claims.get("password", String.class));
    }

    private Key getSignKey() {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
       /* SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64 = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Your Base64 Secret: " + base64);*/
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public Integer extractBookingId(String token) {
        return extractClaim(token, claims -> (Integer) claims.get("id"));
    }



    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateSimpleToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        Optional<Account> user = userRepository.findByEmail(email);
        if(!user.isPresent()){
           return null;
        }
        user.ifPresent(u -> {
            claims.put("email", u.getEmail());
            claims.put("username", u.getUsername());
        });
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.get().getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();

    }
    public String generateBoking(Integer id) {
        Map<String, Object> claims = new HashMap<>();
        Optional<Booking> booking = bookingRepository.findById(id);
        if(!booking.isPresent()){
            return null;
        }
        booking.ifPresent(u -> {
            claims.put("id", u.getId());
        });
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(booking.get().getAccount().getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15 ))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
