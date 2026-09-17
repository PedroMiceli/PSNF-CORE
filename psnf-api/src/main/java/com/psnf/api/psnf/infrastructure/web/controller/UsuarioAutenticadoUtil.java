package com.psnf.api.psnf.infrastructure.web.controller;

import java.util.UUID;

public class UsuarioAutenticadoUtil {

    private static final UUID USUARIO_FIXO_TEMPORARIO = UUID.fromString("3c288400-f58f-4d28-905c-f7dba388d6c9");

    public static UUID obterUsuarioLogadoId() {
        return USUARIO_FIXO_TEMPORARIO;
    }
}
