import os
import csv

SNX_file_directory = "../SNX_files/"


def assemble_snx(
    details,
    manual_harvest_binary,
    two_letter_crop_code,
    m_group_info,
    crop_specific_planting_details,
    harvest_date_if_potatoes_otherwise_n99,
    photo_L_if_soybean_C_otherwise,
    A_if_automatic_planting_R_otherwise,
    A_if_irrigated_N_otherwise,
    D_if_potatoes_M_otherwise,
    irrigation_depth,
    irrigation_threshold,
):
    # Load templates from the txt file
    with open(f"{SNX_file_directory}shared_SNX_template.txt", "r") as file:
        lines = file.readlines()

    # Remove comment lines
    lines = [line for line in lines if not line.startswith("!")]

    # Extract blocks
    templates = {}
    key = None
    block_content = ""
    for line in lines:
        if line.startswith("NEW BLOCK"):
            if key:
                templates[key] = block_content.rstrip()  # Remove trailing whitespace
            key = line.split()[2]
            block_content = ""
        else:
            block_content += line  # Concatenate without adding newlines

    # Handle the last block
    if key:
        templates[key] = block_content.rstrip()  # Remove trailing whitespace

    # Construct SNX content using the extracted blocks
    SNX_contents = "\n".join(
        [
            templates["details_line"].format(details=details),
            templates["general_info_block"],
            templates["factor_level_line"].format(
                manual_harvest_binary=manual_harvest_binary
            ),
            templates["cultivarblock"],
            templates["filename_line"].format(
                two_letter_crop_code=two_letter_crop_code, m_group_info=m_group_info
            ),
            templates["fields_with_placeholder_block_and_planting_details"],
            templates["crop_specific_planting_details_line"].format(
                crop_specific_planting_details=crop_specific_planting_details
            ),
            templates["water_fertilizer_placeholder_block"],
            templates["harvest_details_line"].format(
                harvest_date_if_potatoes_otherwise_n99=harvest_date_if_potatoes_otherwise_n99
            ),
            templates["simulation_controls"],
            templates["simulation_control_line"].format(
                photo_L_if_soybean_C_otherwise=photo_L_if_soybean_C_otherwise
            ),
            templates["management_titles"],
            templates["management_line"].format(
                A_if_automatic_planting_R_otherwise=A_if_automatic_planting_R_otherwise,
                A_if_irrigated_N_otherwise=A_if_irrigated_N_otherwise,
                D_if_potatoes_M_otherwise=D_if_potatoes_M_otherwise,
            ),
            templates["outputs_management_block"],
            templates["irrigation_details_line"].format(
                irrigation_depth=irrigation_depth,
                irrigation_threshold=irrigation_threshold,
            ),
            templates["nitrogen_residues_harvest_block"],
        ]
    )

    return SNX_contents


def generate_snx_content_from_csv(csv_path):
    with open(csv_path, "r") as csv_file:
        csv_reader = csv.DictReader(csv_file)
        for row in csv_reader:
            # Extract values from the current row
            SNX_filename = row["SNX_filename"]
            details = row["details"]
            manual_harvest_binary = row["manual_harvest_binary"]
            two_letter_crop_code = row["two_letter_crop_code"]
            m_group_info = row["m_group_info"]
            crop_specific_planting_details = row["crop_specific_planting_details"]
            harvest_date_if_potatoes_otherwise_n99 = row[
                "harvest_date_if_potatoes_otherwise_n99"
            ]
            photo_L_if_soybean_C_otherwise = row["photo_L_if_soybean_C_otherwise"]
            A_if_automatic_planting_R_otherwise = row[
                "A_if_automatic_planting_R_otherwise"
            ]
            A_if_irrigated_N_otherwise = row["A_if_irrigated_N_otherwise"]
            D_if_potatoes_M_otherwise = row["D_if_potatoes_M_otherwise"]
            irrigation_depth = row["irrigation_depth"]
            irrigation_threshold = row["irrigation_threshold"]

            snx_content = assemble_snx(
                details,
                manual_harvest_binary,
                two_letter_crop_code,
                m_group_info,
                crop_specific_planting_details,
                harvest_date_if_potatoes_otherwise_n99,
                photo_L_if_soybean_C_otherwise,
                A_if_automatic_planting_R_otherwise,
                A_if_irrigated_N_otherwise,
                D_if_potatoes_M_otherwise,
                irrigation_depth,
                irrigation_threshold,
            )

            folder_name = f"{SNX_file_directory}generated_SNX_files"

            # Ensure the folder exists, if not, create it
            if not os.path.exists(folder_name):
                os.makedirs(folder_name)

            # Construct the full file path
            file_path = os.path.join(folder_name, SNX_filename + ".SNX")

            # Write the content to the file
            with open(file_path, "w") as file:
                file.write(snx_content)


def main():
    generate_snx_content_from_csv(f"{SNX_file_directory}data_to_generate_SNX_files.csv")


if __name__ == "__main__":
    main()
