package io.fdlessard.codebites.i18n;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

@SpringBootApplication
@Slf4j
public class SpringBootI18nApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootI18nApplication.class, args);
    }


    class LocaleAwareMessageInterpolator extends ResourceBundleMessageInterpolator {

        private Locale defaultLocale = Locale.ENGLISH;

        public void setDefaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
        }

        @Override
        public String interpolate(final String messageTemplate, final Context context) {
            return interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
        }

        @Override
        public String interpolate(final String messageTemplate, final Context context, final Locale locale) {
            return super.interpolate(messageTemplate, context, locale);
        }
    }


    @Configuration
    class WebMvcConfig extends WebMvcConfigurerAdapter {

        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
            bean.setBasename("classpath:i18n/messages");
            bean.setDefaultEncoding("UTF-8");
            return bean;
        }

        @Bean
        public LocalValidatorFactoryBean validator() {
            LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
            factory.setValidationMessageSource(messageSource());
           // factory.setMessageInterpolator( new LocaleAwareMessageInterpolator());
            return factory;
        }

        @Override
        public Validator getValidator() {
            return validator();
        }
    }


    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setDefaultLocale(Locale.ENGLISH);
        return acceptHeaderLocaleResolver;
    }
/*
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }
*/

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Customer implements Serializable {

        @NotNull(message = "{validation.id.null}")
        private String id;

        @NotNull(message = "{validation.name.null}")
        private String name;

        @DecimalMin(value = "5.00", message = "{validation.amountDue.minimum}")
        private BigDecimal amountDue;
    }

    @RestController
    @RequestMapping(value = "/CustomerController", produces = "application/json")
    class CustomerController {

        @Autowired
        private MessageSource messageSource;

        @GetMapping(value = "/isAlive", produces = "application/json")
        public String isAlive() {
            log.info("CustomerController.isAlive()");

            return messageSource.getMessage("message.hello:", null, Locale.US);

        }

        @GetMapping(value = "/customer/{id}", produces = "application/json")
        public Customer get(@PathVariable String id) {
            log.info("CustomerController.get({})", id);

            return new Customer(id, "toto", new BigDecimal(10.0));
        }

        @PostMapping(value = "/customer", produces = "application/json")
        public void post(@RequestBody @Valid Customer customer) {


            log.info("CustomerController.post({})", customer);
            log.info("CustomerController.post() locale: {}", LocaleContextHolder.getLocale());


        }

    }
}
