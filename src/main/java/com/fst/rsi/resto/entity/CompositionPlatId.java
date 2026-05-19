package com.fst.rsi.resto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompositionPlatId implements Serializable {
    private Long plat;
    private Long ingredient;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositionPlatId)) return false;
        CompositionPlatId that = (CompositionPlatId) o;
        return Objects.equals(plat, that.plat)
                && Objects.equals(ingredient, that.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plat, ingredient);
    }
}