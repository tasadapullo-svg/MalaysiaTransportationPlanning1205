## Data Availability

The reproducibility package for this study is available through three public access points.

1. **Zenodo archived version**  
   DOI: https://doi.org/10.5281/zenodo.19998175

2. **GitHub active development version**  
   Repository: https://github.com/tasadapullo-svg/MalaysiaTransportationPlanning1205.git

3. **Google Drive data folder**  
   Data folder: https://drive.google.com/drive/folders/1lOXrAU_9R6VJRvZZm-m5S_yxfxv4EZK2?usp=sharing

The Google Drive folder contains the supporting information files, figure files, Python cleaning scripts, TomTom processed data, HERE benchmark data, and data summary tables used during manuscript preparation.

Raw API responses are not redistributed because TomTom and HERE platform terms may restrict redistribution of vendor-returned raw responses. The released cleaned and aggregated tables are sufficient to reproduce the reported figures, tables, and benchmark statistics. API keys, account credentials, and private access tokens are excluded.


The cleaned data tables, processing scripts, benchmark scripts, configuration files, figure-reproduction data, and data dictionary required to reproduce the reported figures, tables, and benchmark statistics are archived on Zenodo with DOI: https://doi.org/10.5281/zenodo.19998175.

The active development version and project files are available in the public GitHub repository: https://github.com/tasadapullo-svg/MalaysiaTransportationPlanning1205.git.

The data folders used for manuscript preparation, including the supporting information files, figures, Python cleaning scripts, TomTom processed data, HERE benchmark data, and summary tables, are also available through Google Drive: https://drive.google.com/drive/folders/1lOXrAU_9R6VJRvZZm-m5S_yxfxv4EZK2?usp=sharing.

Raw API responses are not redistributed because TomTom and HERE platform terms may restrict redistribution of vendor-returned raw responses. The released cleaned and aggregated tables are sufficient to reproduce the reported figures, tables, and benchmark statistics. API keys, account credentials, and private access tokens are excluded.

# Pan Borneo Highway (Serian–Simanggang Section)  
## High-Frequency Traffic Flow Data Acquisition & Analysis System

This repository contains the source code, datasets, documentation, and analytical framework for the **Pan Borneo Highway (Serian–Simanggang Section) Traffic Flow High-Frequency Data Acquisition System**.  
The system integrates multi-source real-time traffic APIs (TomTom + HERE), constructs a structured MariaDB database, and provides tools for in-depth congestion and bottleneck analysis.  
:contentReference[oaicite:0]{index=0}  
:contentReference[oaicite:1]{index=1}  

---

## 🔍 1. Project Overview

Due to the lack of fixed traffic detectors (loop detectors, microwave radars) along the Pan Borneo Highway, this project introduces a **software-defined virtual traffic sensing system**.  
A Java-based backend operates 24/7 to collect real-time traffic data at high spatial and temporal resolution.

### 🎯 Key Objectives
- Build the **first high-precision historical traffic flow database** for this corridor  
- Enable analysis of **congestion propagation (shockwaves)**  
- Study speed attenuation under **tourism periods, holidays, rainfall, accidents**  
- Provide data foundations for **traffic planning and roadway management**

---

## 🛰️ 2. Dual-Layer Data Acquisition Strategy  
### “Macro–Micro Collaborative Acquisition Architecture”

### **Micro Layer — TomTom (High-Frequency Section Probe)**
- Functions like **63 virtual microwave detectors**
- API: TomTom Traffic Flow API  
- Mechanism:
  - Fixed monitoring points every ~2 km  
  - 15-minute polling cycle  
- Purpose: capture short-term fluctuations and reconstruct **time–space diagrams**  

### **Macro Layer — HERE (Road Network Operation Scan)**
- Acts as a **Traffic Management Center (TMC)**  
- API: HERE Traffic API + Network Coverage  
- Mechanism:
  - Regional scanning using bounding box  
  - 30-minute update interval  
- Purpose: obtain **Jam Factor**, evaluate overall LOS, validate TomTom microdata

---

## 🗄️ 3. Database Structure

MariaDB 10.3 is used to build a structured and long-term scalable traffic data warehouse.

### **TomTom Tables**
1. `data_redis_internal_combination`  
   - Monitoring point metadata  
2. `data_tomtom_flow`  
   - High-frequency traffic flow values (speed, free-flow speed, timestamps)  
3. `data_here_shape_points`  
   - Detailed road geometry for mapping

### **HERE Tables**
1. `data_here_flow`  
   - Traffic conditions: road name, jam factor, speed  
2. `data_here_shape_points`  
   - Sequence-based geometry of road segments

---

## 🧭 4. Data Analysis Framework

The analytical structure is designed based on transportation planning methodology and the characteristics of the collected dataset.

### **A. Temporal Congestion Analysis**
- Weekly congestion distribution  
- Monthly congestion trends  
- Daily internal traffic fluctuations  
- Tools: heatmaps, time-series plots, moving averages

### **B. Impact Factor Analysis**
- Weather impact (rainy vs clear conditions)  
- Holiday effect  
- Tourism flow impact  
- Tools: boxplots, bar charts, comparative analysis, statistical tests

### **C. Spatial & Operational Traffic Analysis**
- Spatial congestion mapping (GIS)  
- Hotspot detection  
- Functional class congestion comparison  
- Roadway bottleneck identification  
- Segment speed profile charts to detect shockwaves

---

## 📊 5. Insights Enabled by the Data

This dataset allows identification of:

- Recurring congestion hotspots  
- Peak-hour spreading and time shifts  
- Rain-sensitive segments & reduction in roadway capacity  
- Tourism-induced congestion patterns  
- Holiday outbound/return traffic surges  
- Structural bottlenecks (merging areas, intersections, narrow bridges)  
- Potential road capacity deficiencies  

These insights will support roadway planning, operational control strategies, and infrastructure improvement decisions.

---

## 🚀 6. Current Development Status

- ✅ Full backend development completed (Java/Spring Boot)  
- ✅ Full database deployment finished (MariaDB 10.3)  
- ✅ Multi-key load balancing & automatic retry mechanisms implemented  
- 🚧 **Next step:** Run long-term automated data collection for one month  
- 📈 After data accumulation → Full analytical report & congestion modeling

---

## 🗺️ 7. System Visualization Output

- Monitoring point distribution maps  
- Road segment geometry extraction  
- High-resolution time–space diagrams  
- GIS-based congestion heatmaps  
- Segment speed profile charts (for bottleneck detection)

---

## 📚 8. Attached Documents

- **Project Specification PDF**  
- **Data Explanation & Problem Description PDF**

> Documents included in this repository provide full explanations of table structures, API mechanisms, analysis frameworks, and research objectives.


---

## 🙌 10. Acknowledgments
This project integrates TomTom and HERE Technologies traffic APIs within the context of the **Pan Borneo Highway (Serian–Simanggang Section)**.  
It aims to serve as a pioneer of “software-defined traffic sensing” for transportation research and real-time roadway performance monitoring.

---


