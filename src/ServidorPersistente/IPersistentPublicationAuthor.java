/*
 * Created on 27/Out/2004
 */
package ServidorPersistente;

import java.util.List;

/**
 * @author Ricardo Rodrigues
 */
public interface IPersistentPublicationAuthor extends IPersistentObject{

    public List readByPublicationId(Integer publicationId) throws ExcepcaoPersistencia;
    public List readByAuthorId(Integer authorId) throws ExcepcaoPersistencia;
}
