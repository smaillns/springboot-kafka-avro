package com.example.demo.demokafka.core.adapter.db.entity;
import com.example.demo.demokafka.core.domain.model.MyModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "my_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;


    public static MyEntity fromModel(MyModel model) {
        return MyEntity.builder()
                .label(model.getLabel())
                .build();
    }

    public MyModel toModel() {
        return MyModel.builder()
                .id(id)
                .label(label)
                .build();
    }


}
