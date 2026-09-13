# Regras ProGuard/R8 específicas da app AquaVitae.
# Ver a configuração base gerada pelo Android Studio para as regras por omissão.

# Moshi (mantém metadados de Kotlin e classes de modelo geradas por codegen)
-keepclasseswithmembers class * {
    @com.squareup.moshi.FromJson <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keep class pt.aquavitae.android.data.model.** { *; }
