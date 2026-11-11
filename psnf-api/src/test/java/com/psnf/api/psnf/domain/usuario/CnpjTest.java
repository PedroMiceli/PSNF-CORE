package com.psnf.api.psnf.domain.usuario;

import com.psnf.api.psnf.domain.usuario.model.Cnpj;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    @Test
    void aceitaCnpjNumericoComOuSemPontuacao() {
        assertThat(Cnpj.normalizar("11.222.333/0001-81")).isEqualTo("11222333000181");
        assertThat(Cnpj.normalizar(" 11222333000181 ")).isEqualTo("11222333000181");
    }

    @Test
    void aceitaCnpjAlfanumericoDe2026() {
        // exemplo publicado pela Receita Federal
        assertThat(Cnpj.normalizar("12.ABC.345/01DE-35")).isEqualTo("12ABC34501DE35");
        assertThat(Cnpj.normalizar("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
    }

    @Test
    void recusaDigitoErradoFormatoErradoERepetido() {
        assertThatThrownBy(() -> Cnpj.normalizar("11.222.333/0001-80")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.normalizar("12.ABC.345/01DE-3X")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.normalizar("1122233300018")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Cnpj.normalizar("00000000000000")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vazioEhSemCnpjEFormataParaExibir() {
        assertThat(Cnpj.normalizar("")).isNull();
        assertThat(Cnpj.normalizar(null)).isNull();
        assertThat(Cnpj.formatar("12ABC34501DE35")).isEqualTo("12.ABC.345/01DE-35");
    }
}
