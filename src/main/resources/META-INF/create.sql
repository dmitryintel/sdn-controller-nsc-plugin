 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

drop table if exists InspectionHook;
drop table if exists InspectionPort;
drop table if exists NetworkElement;
drop table if exists MacAddress;
drop table if exists PortIp;

create table if not exists InspectionHook (id bigint not null, hookId varchar(255), inspectedPortId varchar(255), inspectionPortId bigint, tag bigint, hookOrder bigint, encType varchar(255), failurePolicyType varchar(255), primary key (id) );
create table if not exists InspectionPort (id bigint not null, inspectionHookId bigint, ingressId bigint, egressId bigint, primary key (id) );

create table if not exists NetworkElement (id bigint not null, elementId bigint, inspectionPortId bigint, primary key (id) );
create table if not exists MacAddress (id bigint not null, elementId bigint, macAddress varchar(128), primary key (id) );
create table if not exists PortIp (id bigint not null, elementId bigint, portIp varchar(128), primary key (id) );

alter table MacAddress add constraint if not exists FK_MAC_ADDRESS_NETWORK_ELEMENT foreign key (elementId) references NetworkElement;
alter table PortIp add constraint if not exists FK_PORT_NETWORK_ELEMENT foreign key (elementId) references NetworkElement; 
alter table NetworkElement add constraint if not exists FK_NETWORK_ELEMENT_INSPECTION_PORT foreign key (inspectionPortId) references InspectionPort;

alter table InspectionPort add constraint if not exists FK_INSPECTION_PORT_INSPECTION_HOOK foreign key (inspectionHookId) references InspectionHook;
alter table InspectionHook add constraint if not exists FK_INSPECTION_HOOK_INSPECTION_PORT foreign key (inspectionPortId) references InspectionPort;

