package com.pos.IDM.services

import com.pos.IDM.persistance.model.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.io.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec


@Service
class JWTService : Serializable {
    companion object {
        private const val secret: String =
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣤⣄⣀⣀⣀⣀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⡿⠟⠉⠉⠉⠉⠉⠉⠉⠀⢀⣀⣠⠤⠶⠶⠶⠶⠦⠤⠤⠄⠈⣉⡉⠉⠉⠉⠛⠛⠻⠿⢿⣿⣿⣶⣶⣶⣤⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⡿⠋⠀⠀⠀⠀⠀⠀⢀⣤⠖⠛⠉⣁⣠⣤⠤⠴⠶⠶⠶⠤⢤⣤⣀⡀⠀⠀⠀⠀⠉⠉⠉⠉⠉⣀⣀⣀⣤⣍⡙⠻⢿⣶⣤⡀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣾⠟⠀⠀⠀⠀⠀⣀⠴⠚⢉⡤⠔⢚⣉⡩⠤⠤⠤⠤⠤⠤⣤⠄⠀⠀⠀⠈⠉⠉⠀⠀⠀⠉⠉⣉⣉⣀⣀⣀⠀⠈⠙⠀⠀⠈⠻⢿⣶⣄⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⢠⣿⠏⠀⠀⠀⢀⡴⠊⠁⣠⠞⣉⡤⠞⠋⠀⠀⠀⠀⠀⠀⠀⠀⠈⢳⡀⠀⠀⠀⠀⠀⠀⠀⠾⢛⡍⠉⠀⠀⠀⠉⠛⠦⣄⠀⠀⠀⠀⠀⠙⣿⡇⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⢠⣿⠏⠀⠀⠀⠀⠈⠀⠠⠞⢁⡞⠉⠀⠀⠀⣀⣀⣀⣀⣀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⢰⠋⠀⠀⠀⠀⠀⠀⠀⠀⠘⢇⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⢀⣴⣿⠟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⣠⣶⣿⣿⣿⣿⣿⣿⠿⠿⠿⣶⣦⣄⠀⠀⠀⠀⠀⠀⠀⠀⢸⠀⠀⣀⣀⣤⣤⣤⣤⣄⠀⠈⠀⠀⠀⠀⠀⠸⣿⣆⠀⠀⠀\n" +
                    "⠀⠀⠀⢀⣴⣿⣿⣿⣖⠒⠦⢄⡀⠀⠀⣤⣤⡖⢀⣾⣿⣭⣿⣿⣿⣿⣿⣿⣤⣄⡀⠈⠙⣿⣷⠀⠀⠀⠀⢀⣀⡀⣠⣴⣿⣿⣿⣿⣯⣭⣿⣿⣷⠄⢤⣄⣀⣀⣀⣀⠈⠻⣷⣤⡀\n" +
                    "⠀⠀⣴⣿⢟⡿⠁⠀⣀⣤⣶⣶⣾⣶⣤⣈⠀⠚⠉⠉⠉⠉⠁⠀⣀⣴⡆⠉⠉⠛⠿⣿⣶⣿⠿⠀⠀⠀⠀⠘⠿⢿⣿⡿⠿⠛⠋⠉⠉⠉⠉⠁⠀⠀⠀⠀⠀⠀⢰⣌⠛⢦⡈⢿⣿\n" +
                    "⠀⣼⡟⢡⡞⠀⢠⣾⡿⠋⠁⠀⣰⡈⠛⠿⣿⣷⣦⣤⣤⣤⣴⣾⠿⠋⠀⠀⠀⠀⠀⠀⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⣀⡀⠀⠀⣠⣶⣶⣶⣶⣶⣄⢹⡇⠙⡆⣿\n" +
                    "⢸⣿⠁⢸⡇⠀⣿⡏⠀⠀⠀⢰⣿⣧⣄⡀⠀⠈⠉⠉⠉⠉⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⢀⢀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣷⣄⡀⠀⠀⠀⠻⢿⣶⣿⣿⠋⣰⡄⠀⠙⠛⠈⡇⠀⡇⣿\n" +
                    "⢸⣿⠀⢸⡇⠀⣿⡄⢠⣤⣶⣿⣿⠛⠻⢿⣷⣤⣄⡀⠀⠀⠀⠀⣀⡤⠤⢤⣤⠾⣴⡿⠿⠿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠉⠻⣿⣦⣄⠀⠀⠀⠀⠀⠀⠀⣿⣇⡀⠀⢀⣠⠇⢀⢧⣿\n" +
                    "⠘⣿⣇⠈⢇⠀⢿⣷⡈⠉⠁⢹⣿⣄⠀⠀⠀⠙⠻⣿⣿⣶⣤⣀⡀⠀⠀⠀⠀⠀⢿⣧⡀⢶⣶⣾⣶⣶⡄⠀⠀⠀⠀⠀⣴⣿⢿⠿⠳⠦⣀⡀⠀⠀⣠⣿⣿⣧⠀⠈⣁⡤⢾⣿⡿\n" +
                    "⠀⠙⣿⣆⠘⠦⣌⣙⠃⠀⠀⠘⢿⣿⣷⣤⣀⡀⠀⣿⣇⠉⠙⠛⠻⠿⢷⣶⣤⣤⣌⣻⠗⠈⠁⠀⠀⠀⠀⠀⣶⣦⣴⣾⠟⠁⠀⠀⠀⠀⠀⢉⣠⣾⣿⣿⣿⣿⠀⠉⠀⢀⣾⡟⠀\n" +
                    "⠀⠀⠈⢻⣷⣦⣤⠉⠁⠀⠀⠀⠀⠹⣿⣟⠻⢿⣷⣿⣿⣦⣀⠀⠀⠀⠀⠈⢙⣿⠿⠿⠿⣷⣶⣶⣤⣤⣤⣀⣈⣉⣉⣁⣀⣀⣀⣀⣤⣤⣶⠿⠻⣿⡏⢻⣿⣿⡆⠀⠀⣾⡟⠀⠀\n" +
                    "⠀⠀⠀⠀⠙⢿⣷⣄⠀⠀⠀⠀⠀⠀⠙⣿⣦⠀⠀⣿⣿⣿⣿⣿⣶⣶⣤⣀⣾⡿⠀⠀⠀⠀⠀⠈⠉⣿⡏⠉⠛⠛⢻⣿⡟⠛⠋⠉⠉⣿⣧⠀⠀⣿⣇⣸⣿⣿⡇⠀⢸⣿⠁⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠹⣿⣇⠀⠀⠀⠀⠀⠀⠈⠻⣷⣤⣼⡿⠀⠈⠙⠛⠿⣿⣿⣿⣷⣶⣶⣤⣤⣀⣀⣀⣿⣇⣀⣀⣀⣀⣿⣇⣀⣀⣤⣤⣼⣿⣶⣾⣿⣿⣿⣿⣿⠇⠀⢸⣿⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⢻⣿⣄⠀⠀⠀⠀⠀⠀⠀⠈⠻⣿⣅⡀⠀⠀⠀⠀⠀⢸⣿⠛⠛⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠀⢸⣿⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠙⣿⣦⡀⠀⠀⠀⠀⠀⠀⠀⠈⠻⢿⣦⣀⠀⠀⢠⣿⠏⠀⠀⠀⠀⠀⠈⢹⣿⠛⠿⠿⠿⠿⣿⣿⣿⡿⣿⣿⣿⣿⢿⣿⡿⢿⣿⢟⣿⡟⠀⠀⢸⣿⡆⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⣷⣦⡀⢀⣄⡀⠀⣀⣀⠀⠀⠉⠻⢿⣶⣿⡏⠀⠀⠀⠀⠀⠀⠀⢸⣿⠀⠀⠀⠀⠀⣾⡟⠀⠀⢀⣿⡟⠀⣼⣿⠁⢼⣿⣾⡿⠁⠀⠀⠈⣿⡇⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⣿⣦⣈⠙⠲⢤⣈⠙⠲⢤⣀⠀⠈⠙⠛⠿⢷⣶⣦⣤⣤⣄⣼⣿⣀⣀⣀⣀⣀⣿⣄⣀⣠⣾⣟⣠⣴⣿⣷⣶⠿⠟⠋⠀⠀⠀⠀⠀⢿⡇⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠿⣿⣦⣄⡉⠓⠦⢤⣈⠑⠲⢤⣄⡀⠀⠈⠉⠉⠉⠛⠛⠛⠛⠛⠛⠛⠛⠛⠛⠛⠛⠛⠛⠋⠉⠉⠉⠀⠀⢀⡆⠀⠀⡀⠀⠀⢸⡇⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⢿⣶⣤⣀⡈⠉⠓⠶⢤⣉⡛⠶⢤⣀⣀⠀⠀⠀⠈⠉⠉⠉⠘⠒⠒⠒⠀⠀⠀⠀⠀⠀⢀⣠⠴⠋⠀⠀⠀⡇⠀⠀⢸⣿⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠛⠻⣿⣶⣦⣀⠀⠈⠉⠓⠒⠢⠭⣭⣀⣀⠀⠉⠉⠉⠉⠉⠉⠀⠒⠒⠒⠋⠉⠉⠀⠀⠀⣠⡴⠚⠁⠀⠀⢸⣿⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠿⣿⣶⣄⡀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠑⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠒⠋⠁⠀⠀⠀⠀⢀⣾⡿⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠿⣷⣦⣤⣤⣤⣄⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣿⡟⠁⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⠛⠿⠿⣿⣶⣶⣤⣀⣀⡀⠀⠀⠀⠀⠀⠀⠀⣀⣠⣤⣶⡿⠟⠁⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠛⠛⠛⠻⠿⠿⠿⠿⠟⠛⢛⢋⠉⡁⠀⠀⠀⠀⠀⠀⠀"

        const val JWT_TOKEN_VALIDITY: Long = 5 * 60 //minutes
        const val ALGORITHM = "HmacSHA256"
    }

    private val secretKey = SecretKeySpec(
        secret.toByteArray(),
        ALGORITHM
    )

    fun generateToken(userID: Long, role: Role): String {

        val now = Instant.now()
        return Jwts.builder()
            .claim("role", role)
            .subject(userID.toString())
            .issuer("com.pos.IDM")
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(JWT_TOKEN_VALIDITY, ChronoUnit.MINUTES)))
            .signWith(secretKey)
            .compact()
    }


    fun parseToken(token: String): Claims {
        val jwt: Jws<Claims> = Jwts.parser()
            .verifyWith(secretKey)
            .requireIssuer("com.pos.IDM")
            .build()
            .parseSignedClaims(token)

        return jwt.payload
    }

}