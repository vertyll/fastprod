package com.vertyll.fastprod.modules.translation.views;

import java.io.Serial;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.security.RolesAllowed;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.i18n.LocalizedText;
import com.vertyll.fastprod.shared.i18n.TranslationCatalogues;
import com.vertyll.fastprod.shared.i18n.TranslationClient;
import com.vertyll.fastprod.shared.i18n.TranslationRow;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

@Route(value = "admin/translations", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public final class TranslationsView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int NOTIFICATION_MS = 3000;
    private static final String ICU_EXAMPLE = "{count, plural, one {# item} other {# items}}";

    private final transient TranslationClient client;
    private final transient TranslationCatalogues catalogues;
    private final Grid<TranslationRow> grid = new Grid<>(TranslationRow.class, false);
    private final Span count = new Span();
    private final TextField search = new TextField();
    private GridListDataView<TranslationRow> rows;

    public TranslationsView(TranslationClient client, TranslationCatalogues catalogues) {
        super();
        this.client = client;
        this.catalogues = catalogues;
        this.rows = grid.setItems(List.of());

        search.setPlaceholder(I18n.t("translations.search"));
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("24rem");
        search.addValueChangeListener(_ -> applyFilter());

        grid.addColumn(TranslationRow::key).setHeader(I18n.t("translations.key")).setAutoWidth(true);
        grid.addColumn(row -> row.messages().pl()).setHeader(I18n.t("translations.polish")).setFlexGrow(1);
        grid.addColumn(row -> row.messages().en()).setHeader(I18n.t("translations.english")).setFlexGrow(1);
        grid.addColumn(row -> row.customized() ? "✓" : "")
            .setHeader(I18n.t("translations.customized"))
            .setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addItemDoubleClickListener(event -> edit(event.getItem()));
        grid.setSizeFull();

        HorizontalLayout toolbar = new HorizontalLayout(new H2(I18n.t("translations.title")), search, count);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        setSizeFull();
        add(toolbar, grid);
        reload();
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.translations");
    }

    private void reload() {
        try {
            rows = grid.setItems(client.list());
            applyFilter();
        } catch (ApiException e) {
            notify(I18n.t("translations.loadFailed", Map.of("reason", I18n.error(e))), NotificationVariant.LUMO_ERROR);
        }
    }

    private void applyFilter() {
        String needle = search.getValue().strip().toLowerCase(Locale.ROOT);
        rows.setFilter(row -> needle.isEmpty() || matches(row, needle));
        count.setText(I18n.t("translations.count", Map.of("count", rows.getItemCount())));
    }

    private static boolean matches(TranslationRow row, String needle) {
        return row.key().toLowerCase(Locale.ROOT).contains(needle)
                || row.messages().pl().toLowerCase(Locale.ROOT).contains(needle)
                || row.messages().en().toLowerCase(Locale.ROOT).contains(needle);
    }

    private void edit(TranslationRow row) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(I18n.t("translations.edit.title", Map.of("key", row.key())));
        dialog.setWidth("40rem");

        TextArea polish = textArea(I18n.t("translations.polish"), row.messages().pl(), row.defaults().pl());
        TextArea english = textArea(I18n.t("translations.english"), row.messages().en(), row.defaults().en());
        Span hint = new Span(I18n.t("translations.edit.hint", Map.of("example", ICU_EXAMPLE)));

        Button save = new Button(I18n.t("common.save"), _ -> {
            try {
                client.update(row.key(), new LocalizedText(polish.getValue(), english.getValue()));
                afterChange(dialog, I18n.t("translations.saved"));
            } catch (ApiException e) {
                notify(I18n.error(e), NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button reset = new Button(I18n.t("translations.reset"), _ -> {
            try {
                client.reset(row.key());
                afterChange(dialog, I18n.t("translations.resetDone"));
            } catch (ApiException e) {
                notify(I18n.error(e), NotificationVariant.LUMO_ERROR);
            }
        });
        reset.setEnabled(row.customized());

        Button cancel = new Button(I18n.t("common.cancel"), _ -> dialog.close());

        dialog.add(new VerticalLayout(polish, english, hint));
        dialog.getFooter().add(reset, cancel, save);
        dialog.open();
    }

    private static TextArea textArea(String label, String value, String defaultText) {
        TextArea area = new TextArea(label);
        area.setValue(value);
        area.setHelperText(I18n.t("translations.default", Map.of("text", defaultText)));
        area.setWidthFull();
        return area;
    }

    private void afterChange(Dialog dialog, String message) {
        dialog.close();
        catalogues.invalidate();
        reload();
        notify(message, NotificationVariant.LUMO_SUCCESS);
    }

    private static void notify(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, NOTIFICATION_MS, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
}
