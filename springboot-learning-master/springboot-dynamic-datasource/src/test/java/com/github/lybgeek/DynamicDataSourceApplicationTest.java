package com.github.lybgeek;

import com.github.lybgeek.dynamic.dto.BookDTO;
import com.github.lybgeek.dynamic.service.BookService;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DynamicDataSourceApplicationTest {

    @Autowired
    private BookService bookService;






    @Test
    public void testAddBook(){
        for(int i = 1; i <= 10; i++){
            BookDTO bookDTO = BookDTO.builder().bookName("spingcloud从入门到精通（第"+i+")版")
                .author("张三"+i).description("springcloud从入门到精通（第"+i+")版,热门系列").price(BigDecimal.valueOf(i*10)).stock(i).build();
            bookDTO = bookService.addBook(bookDTO);
            System.out.println(bookDTO);
        }
    }



    @Test
    public void testListBook(){
        BookDTO bookDTO = new BookDTO();
        bookDTO.setAuthor("张三");
        bookDTO.setBookName("spring");
 //       bookDTO.setId(5L);
        List<BookDTO> bookDTOS = bookService.listBooks(bookDTO);
        if(CollectionUtils.isNotEmpty(bookDTOS)){
            bookDTOS.forEach(book-> System.out.println(book));
        }

    }







}
