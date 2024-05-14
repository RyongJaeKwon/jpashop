package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne) 관계에서의 성능최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */

/**
 * //==쿼리 방식 선택 권장 순서==//
 * 1. 엔티티를 DTO로 변환하는 방법을 선택
 * 2. 필요하면 fetch join으로 성능을 최적화 한다. => 대부분 성능 이슈가 해결된다.
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 선택
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * <문제점>
     * 엔티티를 API 응답으로 외부로 노출하는 것은 좋지않다.
     * FORCE_LAZY_LOADING 이용하면 모든 지연로딩이 걸려있는 쿼리까지 다 나가므로 성능이슈
     * <결론>
     * DTO로 변환해서 반환하는 것이 더 좋은 방법이다.
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();    // order.getMember()까지는 proxy객체이므로 db에 쿼리가 안날라감. 하지만 getName()을 하는순간 쿼리가 나감 -> Lazy 강제 초기화
            order.getDelivery().getAddress();    // Lazy 강제 초기화
        }
        return all;
    }

    /**
     * 엔티티를 조회해서 DTO로 변환 (fetch join 사용X)
     * <문제점>
     * 지연로딩으로 인한 쿼리가 N번 호출됨 (N + 1) 문제
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        // SQL 1번에 ORDER 2개 조회됨
        // N + 1 문제 -> 1 + 회원 N + 배송 N
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * JPA에서 DTO로 바로 조회
     * 쿼리 1번 호출
     * select 절에서 원하는 데이터만 선택해서 조회
     * <단점>
     *     repository가 API 스펙에 맞춘 코드를 포함하게 됨.
     *     재사용성이 떨어짐. , 네트웤 성능이슈도 미미함.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
