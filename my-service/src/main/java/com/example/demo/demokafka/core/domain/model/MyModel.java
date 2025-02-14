package com.example.demo.demokafka.core.domain.model;

import com.example.demo.demokafka.event.MyEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyModel {

    private Long id;
    private String label;

    public boolean isValid() {
        return id != 0;
    }

    /**
     * Converts a MyEvent to a MyModel.
     *
     * Note: This method is used due to the complexity of the mapping.
     * Consider using a dedicated mapper class to adhere to the Single Responsibility Principle.
     */
    public static MyModel fromEvent(MyEvent event) {
        return MyModel.builder()
                    .id(event.getId())
                    .label(event.getLabel())
                    .build();
    }

    public MyEvent toEvent()  {
        return MyEvent.newBuilder()
                .setId(id)
                .setLabel(label)
                .build();
    }
}
