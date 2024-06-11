package com.shirn.veggies.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(value = "jwt")
class JwtConfig(
    var secret: Secret? = null
)

class Secret(
    var value: String? = null,
    var alg: String? = null
)