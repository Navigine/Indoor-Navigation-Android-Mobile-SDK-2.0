package com.navigine.navigine.demo.utils;


import com.navigine.navigine.demo.R;
import com.navigine.navigine.demo.models.VenueIconObj;

import java.util.ArrayList;
import java.util.List;

public class VenueIconsListProvider {

    private VenueIconsListProvider() {}

    public static List<VenueIconObj> VenueIconsList = new ArrayList<>();

    static {
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_lift, "Lifts"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_stairs, "Stairs"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_cosmetics, "Cosmetics"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_jewellery, "Jewellery"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_gifts, "Gifts"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_toilet, "Toilet"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_cloakroom, "Cloakrooms"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_traveller_goods, "Traveller"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_meeting_rooms, "Meeting"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_police, "Police"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_services, "Services"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_wifi, "Wifi"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_train, "Train"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_general, "General"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_escalator, "Escalator"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_car_services, "Car"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_atm_banks, "Banking"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_florists, "Florists"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_pets, "Pets"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_footwear, "Footwear"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_food, "Cafes"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_beauty, "Hairdressing & Beauty"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_homeware, "Homeware"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_leisure, "Leisure"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_music, "Music"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_newsagents, "Newsagents"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_optometrists, "Optometrists"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_phones, "Phones"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_pharmacies, "Pharmacies"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_supermarket, "Supermarket"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_clothing, "Clothing"));
        VenueIconsList.add(new VenueIconObj(R.drawable.ic_venue_children_room, "Children"));
    }
}