import org.gradle.nativeplatform.internal.DefaultTargetMachineFactory

rootProject.name = "httpbin"

@CacheableRule
abstract class Brotli4JRule : ComponentMetadataRule {
    data class NativeVariant(val os: String, val arch: String, val classifier: String) {
        fun dependency(version: String) = "com.aayushatharva.brotli4j:native-${classifier}:${version}"
    }

    private val nativeVariants = listOf(
        NativeVariant(OperatingSystemFamily.WINDOWS, "aarch64", "windows-aarch64"),
        NativeVariant(OperatingSystemFamily.WINDOWS, "x86-64", "windows-x86_64"),
        NativeVariant(OperatingSystemFamily.MACOS, "x86-64", "osx-x86_64"),
        NativeVariant(OperatingSystemFamily.MACOS, "aarch64", "osx-aarch64"),
        NativeVariant(OperatingSystemFamily.LINUX, "x86-64", "linux-x86_64"),
        NativeVariant(OperatingSystemFamily.LINUX, "aarch64", "linux-aarch64"),
        NativeVariant(OperatingSystemFamily.LINUX, "arm-v7", "linux-armv7"),
        NativeVariant(OperatingSystemFamily.LINUX, "s390x", "linux-s390x"),
        NativeVariant(OperatingSystemFamily.LINUX, "riscv64", "linux-riscv64"),
        NativeVariant(OperatingSystemFamily.LINUX, "ppc64le", "linux-ppc64le"),
    )

    @get:Inject
    abstract val objects: ObjectFactory

    override fun execute(context: ComponentMetadataContext) {
        listOf("compile", "runtime").forEach { base -> addVariant(context, base) }
    }

    private fun addVariant(context: ComponentMetadataContext, base: String) {
        val version = context.details.id.version

        nativeVariants.forEach { variant ->
            context.details.addVariant("${variant.classifier}-${base}", base) {
                attributes {
                    attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(variant.os))
                    attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(variant.arch))
                }

                withDependencies {
                    add(variant.dependency(version))
                }
            }
        }

        context.details.addVariant("all-${base}", base) {
            attributes {
                attributes.attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named("all"))
                attributes.attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named("all"))
            }

            withDependencies {
                nativeVariants.forEach { variant ->
                    add(variant.dependency(version))
                }
            }
        }
    }
}

gradle.beforeProject {
    val host = DefaultTargetMachineFactory(objects).host()

    // This tells gradle that we want an artifact with the correct OperatingSystemFamily and MachineArchitecture
    // Gradle will then attempt to find the artifact with the best matching value, or if one cannot be found,
    // fall back to the default (i.e. in the case of any dependency other than brotli4j)
    configurations.configureEach {
        // if the configuration is being published, don't set the attributes
        if (!isCanBeResolved)
            return@configureEach

        attributes {
            if (OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE !in keySet())
                attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, host.operatingSystemFamily)
            if (MachineArchitecture.ARCHITECTURE_ATTRIBUTE !in keySet())
                attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, host.architecture)
        }
    }

    // can be removed if not using shadow plugin
    pluginManager.withPlugin("com.gradleup.shadow") {
        val runtimeClasspath by configurations.getting

        configurations.register("shaded") {
            extendsFrom(runtimeClasspath)

            isCanBeConsumed = false
            isCanBeResolved = true
            isCanBeDeclared = true

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))

                attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named("all"))
                attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named("all"))
            }
        }
    }
}

dependencyResolutionManagement {
    components {
        withModule<Brotli4JRule>("com.aayushatharva.brotli4j:natives")
    }
}
