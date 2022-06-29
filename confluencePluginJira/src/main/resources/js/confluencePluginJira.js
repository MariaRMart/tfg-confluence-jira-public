// Código adaptado de https://www.w3schools.com/howto/howto_js_filter_table.asp
// y https://www.w3schools.com/howto/howto_js_sort_table.asp
function searchTable() {
    var input, filter, found, table, tr, td, i, j;
    input = document.getElementById("myInput");
    filter = input.value.toUpperCase();
    table = document.getElementById("myTable");
    tr = table.getElementsByTagName("tr");

    for (i = 0; i < tr.length; i++) {
        td = tr[i].getElementsByTagName("td");

        for (j = 0; j < td.length; j++) {
            if (td[j].textContent.toUpperCase().indexOf(filter) > -1) {
                found = true;
            }
        }
        if (found) {
            tr[i].style.display = "";
            found = false;
        } else if (tr[i].id != 'tableHeader') {
            tr[i].style.display = "none";
        }
    }
}

function sortTable(n) {
    var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
    table = document.getElementById("myTable");
    switching = true;
    dir = "asc";
    const priorities = ["Lowest", "Low", "Medium", "High", "Highest"];
    const estados = ["Por hacer", "En progreso", "Listo"];

    while (switching) {
        switching = false;
        rows = table.rows;
        for (i = 1; i < (rows.length - 1); i++) {
            shouldSwitch = false;
            x = rows[i].getElementsByTagName("TD")[n];
            y = rows[i + 1].getElementsByTagName("TD")[n];
            let doBreak = false;
            if (dir == "asc") { ///////////////////
                switch(n) {
                    case 0:
                        let x_type = x.getElementsByTagName("img")[0].title;
                        let y_type = y.getElementsByTagName("img")[0].title;
                        if (x_type > y_type) {
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 5:
                        let x_prio = priorities.indexOf(x.getElementsByTagName("img")[0].title);
                        let y_prio = priorities.indexOf(y.getElementsByTagName("img")[0].title);
                        if (x_prio > y_prio){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 6:
                        let x_est = estados.indexOf(x.textContent);
                        let y_est = estados.indexOf(y.textContent);
                        if (x_est > y_est){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 8 || 9:
                        if (new Date(x.textContent) > new Date(y.textContent)) {
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    default:
                        if (x.textContent.toLowerCase() > y.textContent.toLowerCase()){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                }
                if (doBreak) break;
            } else if (dir == "desc") { /////////////////
                switch(n) {
                    case 0:
                        let x_type = x.getElementsByTagName("img")[0].title;
                        let y_type = y.getElementsByTagName("img")[0].title;
                        if (x_type < y_type){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 5:
                        let x_prio = priorities.indexOf(x.getElementsByTagName("img")[0].title);
                        let y_prio = priorities.indexOf(y.getElementsByTagName("img")[0].title);
                        if (x_prio < y_prio){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 6:
                        let x_est = estados.indexOf(x.textContent);
                        let y_est = estados.indexOf(y.textContent);
                        if (x_est < y_est){
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    case 8 || 9:
                        if (new Date(x.textContent) < new Date(y.textContent)) {
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                    default:
                        if (x.textContent.toLowerCase() < y.textContent.toLowerCase()) {
                            shouldSwitch = true;
                            doBreak = true;
                        }
                        break;
                }
                if (doBreak) break;
            }
        }
        if (shouldSwitch) {
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            switchcount++;
        } else {
            if (switchcount == 0 && dir == "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
}

