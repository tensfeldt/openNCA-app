SELECT_ALL_ENGINES=\
	select ee.id, ee.name, ee.version, ee.cntnr_type as type, ee.repository_url as repositoryUrl, est.SCRIPT_TYPE as scriptType, \
	est.command, ee.disabled, ee.description \
	from EQUIP_OWNER.EXECUTION_ENGINE ee \
	LEFT OUTER JOIN EQUIP_OWNER.EE_SCRIPT_TYPE est \
	ON ee.ID = est.ID \


SELECT_ENGINE_BY_ID=\
	select ee.id, ee.name, ee.version, ee.cntnr_type as type, ee.repository_url as repositoryUrl, est.SCRIPT_TYPE as scriptType, \
	est.command, ee.disabled, ee.description \
	from EQUIP_OWNER.EXECUTION_ENGINE ee \
	JOIN EQUIP_OWNER.EE_SCRIPT_TYPE est \
	ON est.ID = ee.ID \
	and ee.ID = ?

SELECT_ENGINE_BY_NAME=\
	select ee.id, ee.name, ee.version, ee.cntnr_type as type, ee.repository_url as repositoryUrl, est.SCRIPT_TYPE as scriptType, \
	est.command, ee.disabled, ee.description \
	from EQUIP_OWNER.EXECUTION_ENGINE ee \
	JOIN EQUIP_OWNER.EE_SCRIPT_TYPE est \
	ON est.ID = ee.ID \
	and ee.NAME = ? and ee.disabled = 'F'