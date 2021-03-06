package org.example.ldap.dao;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.example.ldap.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Description: spring-boot2
 * User: yuanct
 * Date: 2019/1/11 2:08 PM
 */
@SpringBootTest
@Slf4j
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    public void findAll() {
//        personRepository.findAll().forEach(System.out::println);
        Iterable<Person> personIterable = personRepository.findAll();
        List<Person> personList = Lists.newArrayList(personIterable);
        log.info("personList size:{}", personList.size());
        for (Person person : personList) {
            log.info(person.toString());
        }
    }

    @Test
    public void save() {
        Person person = new Person();
        person.setUid("uid:1");
        person.setSuerName("AAA");
        person.setCommonName("aaa");
        person.setUserPassword("123456");
        personRepository.save(person);
        personRepository.findAll().forEach(System.out::println);
    }

}