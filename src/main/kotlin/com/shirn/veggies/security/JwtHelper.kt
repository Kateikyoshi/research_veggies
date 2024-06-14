package com.shirn.veggies.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.stereotype.Component

@Component
class JwtHelper(
    private val jwtConfig: JwtConfig
) {

    fun generateJwt(scp: String): String {

        val secret = jwtConfig.secret ?: throw IllegalStateException("jwt config secret can't be null")
        val secretValue = secret.value ?: throw IllegalStateException("jwt config secret value can't be null")
        val secretAlg = secret.alg ?: throw IllegalStateException("jwt config secret alg can't be null")

        val alg = if (secretAlg == "HS256") {
            Algorithm.HMAC256(secretValue)
        } else throw IllegalStateException("jwt secret alg $secretAlg is not supported")

        return JWT.create()
            .withIssuer("senechal")
            .withAudience("veggies")
            .withClaim("scp", scp) //by default will be mapped automatically into SCOPE_<name> authority
            .sign(alg)
    }
}