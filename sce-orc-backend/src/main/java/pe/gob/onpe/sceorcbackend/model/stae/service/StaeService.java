package pe.gob.onpe.sceorcbackend.model.stae.service;

import pe.gob.onpe.sceorcbackend.model.stae.dto.DocumentoElectoralRequest;
import pe.gob.onpe.sceorcbackend.model.stae.dto.ResultadoPs;


public interface StaeService {

	ResultadoPs  insertActaStae(
			String piEsquema,
			boolean esDesarollo,
			String piActa,
			String usuario
	);
	
	ResultadoPs  insertListaElectoresStae(
			String piEsquema,
			boolean esDesarollo,
			String piLe,
			String usuario
	);
	
	void guardarDocumentosElectorales(DocumentoElectoralRequest request, String usuario);
	
	boolean validarTokenStae(String tokenBearer, String numeroMesa);
	
	void sendProcessActaStae(
			String numMesa,
			Integer tipoEleccion,
			String codUsuario,
			String codCentroComput
			);

}
