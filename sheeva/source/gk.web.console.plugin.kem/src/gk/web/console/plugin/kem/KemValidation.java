package gk.web.console.plugin.kem;

public class KemValidation {
	public static final String adapterList[] = { "GentosAdapter", "EmersonAdapter", "EntesAdapter", "EnergetixPowerMeterAdapter", "BrainchildAdapter",
			"JanitzaAdapter", "ContrecAdapter", "RielloAdapter", "EnergetixSensorAdapter", "GeAquatransAdapter", "BaylanAdapter", "SiemensAdapter",
			"DentAdapter", "PhidgetsAdapter", "SitelabAdapter", "SocomecAdapter", "BacnetAdapter", "OpcAdapter", "ModbusConverterAdapter",
			"AquametroAdapter", "KamAdapter", "DaikinAdapter" , "EpowerAdapter","SchneiderAdapter", "BoschAdapter" };

	public static boolean isValidatedAdapterName(String adapterName) {
		if ((adapterName != null) && (adapterName.contains("Adapter"))) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidatedBundleName(String bundleName) {
		return true;
	}
}
