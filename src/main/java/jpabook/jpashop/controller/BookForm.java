package jpabook.jpashop.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "상품명은 필수 입니다.")
    private String name;

    @NotNull(message = "상품가격은 필수 입니다.")
    private Integer price;

    @NotNull(message = "상품수량은 필수 입니다.")
    private Integer stockQuantity;

    private String author;
    private String isbn;
}
