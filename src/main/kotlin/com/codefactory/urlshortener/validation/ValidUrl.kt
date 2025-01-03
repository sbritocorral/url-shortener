package com.codefactory.urlshortener.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UrlValidator::class])
annotation class ValidUrl(
    val message: String = "Invalid URL format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
