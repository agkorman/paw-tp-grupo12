package ar.edu.itba.paw.persistence;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class JpaTestFlusher {

    @PersistenceContext
    private EntityManager em;

    public void flushAndClear() {
        try {
            em.flush();
        } finally {
            em.clear();
        }
    }
}
