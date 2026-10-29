alter table userservice.consultant
    add registration_url varchar(500) null after notifications_settings,
    add registration_url_added_date datetime null after registration_url;
