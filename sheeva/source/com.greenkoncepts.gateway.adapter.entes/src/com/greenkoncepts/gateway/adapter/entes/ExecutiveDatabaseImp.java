package com.greenkoncepts.gateway.adapter.entes;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;

public class ExecutiveDatabaseImp extends AExecutiveDatabase {

	ExecutiveDatabaseImp(String adapter) {
		super(adapter);
	}

	@Override
	public void initTables() {
		createAdapterTable();
		createDeviceTable();
	}

}
