package site.persistent.entity.recipes;

import jakarta.persistence.*;

@Entity
@Table(name = "instruction_steps")
public class InstructionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    // Конструктор по умолчанию
    public InstructionStep() {
    }

    public InstructionStep(String text) {
        this.text = text;
    }

    // Геттеры и Сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}