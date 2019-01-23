Mastership and Auto-Scaling Management System (MAS-Man)
========================================

### Overview

* All source codes and scripts are totally implemented by Woojoong Kim @ POSTECH / ONF

* JAVA-based application

* Focusing on ONOS controllers (fully tested under ONOS 1.10)

* Only support CLI for user interaction

### Preliquisites

* ONOS Control Plane Monitors
 * <https://github.com/woojoong88/onos-provider-messagert>: need to place it into OpenFlow provider layer
 * <https://github.com/woojoong88/onos-app-cpmanrt>: need to place it into App layer

* Focusing on one or more Mininet machines

* ONOS controllers need to be running with Oracle VM VirtualBox

* MAS-Man requires the permission to access each machine with root account
 * Types of machine: physical machine and virtual machine
  * Physical machines to run ONOS controllers and Mininet
  * Virtual machines running ONOS

### How to configure?

* Prepare config.json file in this project root directory
* Write below file

```
{
    "Controllers":
    [
        {
            "name": "as-onos-1",
            "controllerId": "192.168.200.101",
            "ipAddr": "192.168.200.101",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-2",
            "controllerId": "192.168.200.102",
            "ipAddr": "192.168.200.102",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-3",
            "controllerId": "192.168.200.103",
            "ipAddr": "192.168.200.103",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-4",
            "controllerId": "192.168.200.104",
            "ipAddr": "192.168.200.104",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-5",
            "controllerId": "192.168.200.105",
            "ipAddr": "192.168.200.105",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-6",
            "controllerId": "192.168.200.106",
            "ipAddr": "192.168.200.106",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-7",
            "controllerId": "192.168.200.107",
            "ipAddr": "192.168.200.107",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-8",
            "controllerId": "192.168.200.108",
            "ipAddr": "192.168.200.108",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        },
        {
            "name": "as-onos-9",
            "controllerId": "192.168.200.109",
            "ipAddr": "192.168.200.109",
            "restPort": "8181",
            "sshPort": "22",
            "controllerGuiId": "onos",
            "controllerGuiPw": "rocks",
            "sshId": "sdn",
            "sshPw": "onos",
            "sshRootId": "root",
            "sshRootPw": "onos",
            "numMinCPU": "2",
            "numMaxCPU": "18"
        }
    ],
    "PMs":
    [
        {
            "ipAddr": "192.168.200.31",
            "sshPort": "22",
            "sshId": <SSH_ID>,
            "sshPw": <SSH_PW>,
            "sshRootId": "root",
            "sshRootPw": <ROOT_PW>
        }
    ],
    "Relationships":
    [
        {
            "PMIpAddr": "192.168.200.31",
            "Controllers":
            [
                {
                    "name": "as-onos-1",
                    "controllerId": "192.168.200.101"
                },
                {
                    "name": "as-onos-2",
                    "controllerId": "192.168.200.102"
                },                {
                    "name": "as-onos-3",
                    "controllerId": "192.168.200.103"
                },
                {
                    "name": "as-onos-4",
                    "controllerId": "192.168.200.104"
                },
                {
                    "name": "as-onos-5",
                    "controllerId": "192.168.200.105"
                },
                {
                    "name": "as-onos-6",
                    "controllerId": "192.168.200.106"
                },
                {
                    "name": "as-onos-7",
                    "controllerId": "192.168.200.107"
                },
                {
                    "name": "as-onos-8",
                    "controllerId": "192.168.200.108"
                },
                {
                    "name": "as-onos-9",
                    "controllerId": "192.168.200.109"
                }
            ]
        }
    ],
    "Mininet":
    [
      {
        "ipAddr": "192.168.200.201",
        "sshId": <SSH_ID>,
        "sshPw": <SSH_PW>,
        "sshRootId": "root",
        "sshRootPw": <ROOT_PW>,
        "switches":
        [
          {"id": "s1_0", "dpid": "of:0100000000060000"},
          {"id": "s1_1", "dpid": "of:0100000000060001"},
          {"id": "s1_2", "dpid": "of:0100000000060002"},
          {"id": "s1_3", "dpid": "of:0100000000060100"},
          {"id": "s1_4", "dpid": "of:0100000000060101"},
          {"id": "s1_5", "dpid": "of:0100000000060102"},
          {"id": "s1_6", "dpid": "of:0100000000060200"},
          {"id": "s1_7", "dpid": "of:0100000000060201"},
          {"id": "s1_8", "dpid": "of:0100000000060202"},
          {"id": "s1_9", "dpid": "of:0100000000000001"},
          {"id": "s1_10", "dpid": "of:0100000000000101"},
          {"id": "s1_11", "dpid": "of:0100000000000201"},
          {"id": "s1_12", "dpid": "of:0100000000000301"},
          {"id": "s1_13", "dpid": "of:0100000000000401"},
          {"id": "s1_14", "dpid": "of:0100000000000501"},
          {"id": "s1_15", "dpid": "of:0100000000010001"},
          {"id": "s1_16", "dpid": "of:0100000000010101"},
          {"id": "s1_17", "dpid": "of:0100000000010201"},
          {"id": "s1_18", "dpid": "of:0100000000010301"},
          {"id": "s1_19", "dpid": "of:0100000000010401"},
          {"id": "s1_20", "dpid": "of:0100000000010501"},
          {"id": "s1_21", "dpid": "of:0100000000020001"},
          {"id": "s1_22", "dpid": "of:0100000000020101"},
          {"id": "s1_23", "dpid": "of:0100000000020201"},
          {"id": "s1_24", "dpid": "of:0100000000020301"},
          {"id": "s1_25", "dpid": "of:0100000000020401"},
          {"id": "s1_26", "dpid": "of:0100000000020501"},
          {"id": "s1_27", "dpid": "of:0100000000030001"},
          {"id": "s1_28", "dpid": "of:0100000000030101"},
          {"id": "s1_29", "dpid": "of:0100000000030201"},
          {"id": "s1_30", "dpid": "of:0100000000030301"},
          {"id": "s1_31", "dpid": "of:0100000000030401"},
          {"id": "s1_32", "dpid": "of:0100000000030501"},
          {"id": "s1_33", "dpid": "of:0100000000040001"},
          {"id": "s1_34", "dpid": "of:0100000000040101"},
          {"id": "s1_35", "dpid": "of:0100000000040201"},
          {"id": "s1_36", "dpid": "of:0100000000040301"},
          {"id": "s1_37", "dpid": "of:0100000000040401"},
          {"id": "s1_38", "dpid": "of:0100000000040501"},
          {"id": "s1_39", "dpid": "of:0100000000050001"},
          {"id": "s1_40", "dpid": "of:0100000000050101"},
          {"id": "s1_41", "dpid": "of:0100000000050201"},
          {"id": "s1_42", "dpid": "of:0100000000050301"},
          {"id": "s1_43", "dpid": "of:0100000000050401"},
          {"id": "s1_44", "dpid": "of:0100000000050501"}
        ]
      },
      {
        "ipAddr": "192.168.200.202",
        "sshId": <SSH_ID>,
        "sshPw": <SSH_PW>,
        "sshRootId": "root",
        "sshRootPw": <ROOT_PW>,
        "switches":
        [
          {"id": "s1_0", "dpid": "of:0200000000060000"},
          {"id": "s1_1", "dpid": "of:0200000000060001"},
          {"id": "s1_2", "dpid": "of:0200000000060002"},
          {"id": "s1_3", "dpid": "of:0200000000060100"},
          {"id": "s1_4", "dpid": "of:0200000000060101"},
          {"id": "s1_5", "dpid": "of:0200000000060102"},
          {"id": "s1_6", "dpid": "of:0200000000060200"},
          {"id": "s1_7", "dpid": "of:0200000000060201"},
          {"id": "s1_8", "dpid": "of:0200000000060202"},
          {"id": "s1_9", "dpid": "of:0200000000000001"},
          {"id": "s1_10", "dpid": "of:0200000000000101"},
          {"id": "s1_11", "dpid": "of:0200000000000201"},
          {"id": "s1_12", "dpid": "of:0200000000000301"},
          {"id": "s1_13", "dpid": "of:0200000000000401"},
          {"id": "s1_14", "dpid": "of:0200000000000501"},
          {"id": "s1_15", "dpid": "of:0200000000010001"},
          {"id": "s1_16", "dpid": "of:0200000000010101"},
          {"id": "s1_17", "dpid": "of:0200000000010201"},
          {"id": "s1_18", "dpid": "of:0200000000010301"},
          {"id": "s1_19", "dpid": "of:0200000000010401"},
          {"id": "s1_20", "dpid": "of:0200000000010501"},
          {"id": "s1_21", "dpid": "of:0200000000020001"},
          {"id": "s1_22", "dpid": "of:0200000000020101"},
          {"id": "s1_23", "dpid": "of:0200000000020201"},
          {"id": "s1_24", "dpid": "of:0200000000020301"},
          {"id": "s1_25", "dpid": "of:0200000000020401"},
          {"id": "s1_26", "dpid": "of:0200000000020501"},
          {"id": "s1_27", "dpid": "of:0200000000030001"},
          {"id": "s1_28", "dpid": "of:0200000000030101"},
          {"id": "s1_29", "dpid": "of:0200000000030201"},
          {"id": "s1_30", "dpid": "of:0200000000030301"},
          {"id": "s1_31", "dpid": "of:0200000000030401"},
          {"id": "s1_32", "dpid": "of:0200000000030501"},
          {"id": "s1_33", "dpid": "of:0200000000040001"},
          {"id": "s1_34", "dpid": "of:0200000000040101"},
          {"id": "s1_35", "dpid": "of:0200000000040201"},
          {"id": "s1_36", "dpid": "of:0200000000040301"},
          {"id": "s1_37", "dpid": "of:0200000000040401"},
          {"id": "s1_38", "dpid": "of:0200000000040501"},
          {"id": "s1_39", "dpid": "of:0200000000050001"},
          {"id": "s1_40", "dpid": "of:0200000000050101"},
          {"id": "s1_41", "dpid": "of:0200000000050201"},
          {"id": "s1_42", "dpid": "of:0200000000050301"},
          {"id": "s1_43", "dpid": "of:0200000000050401"},
          {"id": "s1_44", "dpid": "of:0200000000050501"}
        ]
      }
    ]
}
```
