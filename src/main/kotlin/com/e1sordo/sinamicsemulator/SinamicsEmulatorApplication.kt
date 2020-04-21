package com.e1sordo.sinamicsemulator

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import java.util.*


@SpringBootApplication
@Configuration
class SinamicsEmulatorApplication {

    @Value("classpath:data.csv") // or @Value("file:#{systemProperties.mapping}")
    private val file: Resource? = null

    @Bean
    fun getMapping(): Map<Int, List<String>> {
        val mapping = hashMapOf<Int, List<String>>()

        Scanner(file!!.inputStream).use { sc ->
            while (sc.hasNextLine()) {
                val nextLine = sc.next() ?: break
                val values = nextLine.split(",")
                mapping[values[0].toInt()] = values.subList(1,5)
            }
        }
        return mapping
    }

//        try (val sc = Scanner(file.getInputStream())) {
//            while (sc.hasNextLine()) {
//                mapping.put(sc.nextInt(), sc.next().charAt(0));
//            }
//        } catch (IOException e) {
//            logger.error("could not load mapping file", e)
//        }
//            return mapping;
//        }


    @Bean
    fun corsConfigurer(): WebMvcConfigurer? {
        return object : WebMvcConfigurerAdapter() {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
            }
        }
    }

    @Bean
    fun run(repository: DriveParameterRepository) = ApplicationRunner {
        repository.save(DriveParameter(paramId = 1075, paramIndex = 0, value = "2900.0", dataType = "Uint"))
        repository.save(DriveParameter(paramId = 795, paramIndex = 0, value = "1", dataType = "Uint"))
        repository.save(DriveParameter(paramId = 796, paramIndex = 0, value = "1", dataType = "Uint"))

        repository.save(DriveParameter(paramId = 2000, paramIndex = 0, value = "1500", dataType = "Float")) // Референсное значение скорости
        repository.save(DriveParameter(paramId = 2900, paramIndex = 0, value = "40", dataType = "Float")) // Задаваемая скорость

        repository.save(DriveParameter(paramId = 21, paramIndex = 0, value = "0", dataType = "Float")) // Текущая скорость на двигателе
        repository.save(DriveParameter(paramId = 27, paramIndex = 0, value = "0", dataType = "Float")) // Текущее напряжение на двигателе
    }
}

fun main(args: Array<String>) {
    runApplication<SinamicsEmulatorApplication>(*args)
}
