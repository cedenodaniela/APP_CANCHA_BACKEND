package com.canchapp.horarios.domain;

/**
 * Puerto para comprobar que una cancha existe antes de bloquearla. Lo implementa un adaptador que
 * usa los casos de uso del módulo {@code canchas}, sin tocar su infraestructura (AGENTS.md §4).
 */
public interface CanchasDelComplejo {

    boolean existe(Long canchaId);
}
