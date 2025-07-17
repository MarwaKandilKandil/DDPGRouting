# DDPGRouting

DDPGRouting is a Software-Defined Networking (SDN) routing application developed for the [ONOS controller](https://onosproject.org/) and [Mininet network emulator](http://mininet.org/). It leverages Deep Reinforcement Learning (DRL), specifically the Double Prioritized Deep Deterministic Policy Gradient (Double-PDDPG) algorithm, to optimize routing decisions dynamically in SDN environments.

This project is part of an ongoing research effort to improve adaptive and intelligent routing in programmable networks.

## 📁 Project Structure

The repository is organized into the following folders:

- **DoublePDDPGRouting/**  
  Contains the ONOS project source files including:
  - Build files 
  - Custom SDN routing application code
  - DRL framework implementation based on Double-PDDPG

- **Installation and Connection/**  
  Includes PDF guides detailing:
  - Step-by-step installation of ONOS and Mininet
  - Configuration and setup to connect the controller and the emulator

- **Mininet_Topologies/**  
  Provides example Python scripts to run different network topologies in Mininet, which are used to evaluate and test the routing model.

- **Model Diagrams/**  
  A collection of visual diagrams illustrating:
  - The system architecture
  - DRL agent workflow
  - ONOS and Mininet integration
  

## 🔬 Research Context

This work is part of a research project supported and funded by **Kuwait University Research Grant number EO01/22**. It investigates the integration of deep reinforcement learning into SDN routing frameworks, aiming to enable scalable, adaptive, and intelligent routing solutions.

## 👩‍🔬 Author Contributions

| Author | Contributions |
|--------|---------------|
| **Marwa Kandil** | Methodology, Software, Investigation, Writing – Original Draft, Visualization |
| **Mohamad Khattar Awad** | Conceptualization, Validation, Formal Analysis, Writing – Review & Editing, Supervision, Project Administration |
| **Eiman Mohammed Alotaibi** | Conceptualization, Formal Analysis, Writing – Review & Editing, Project Administration |
| **Reza Mohammadi** | Software, Methodology, Validation |

## 📜 Citation

This repository is shared for academic and research purposes. Please cite appropriately if used in any publication. 

## 📫 Contact

For inquiries or collaborations, please contact Marwa Kandil at [marwa.kandil@live.com].
